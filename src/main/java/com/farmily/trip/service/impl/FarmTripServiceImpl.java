package com.farmily.trip.service.impl;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.farmily.trip.dto.CommentCreateRequest;
import com.farmily.trip.dto.CommentResponse;
import com.farmily.trip.dto.OrderCreateRequest;
import com.farmily.trip.dto.OrderResponse;
import com.farmily.trip.dto.SessionCreateRequest;
import com.farmily.trip.dto.SessionResponse;
import com.farmily.trip.dto.TripCreateRequest;
import com.farmily.trip.dto.TripDetailResponse;
import com.farmily.trip.dto.TripListResponse;
import com.farmily.trip.dto.TripReviewRequest;
import com.farmily.trip.model.FarmTrip;
import com.farmily.trip.model.FarmTripComment;
import com.farmily.trip.model.FarmTripOrder;
import com.farmily.trip.model.FarmTripSession;
import com.farmily.trip.model.FarmTripType;
import com.farmily.trip.model.OrderStatus;
import com.farmily.trip.model.SessionStatus;
import com.farmily.trip.model.TripStatus;
import com.farmily.trip.repository.FarmTripAuditsRepository;
import com.farmily.trip.repository.FarmTripCommentRepository;
import com.farmily.trip.repository.FarmTripOrderRepository;
import com.farmily.trip.repository.FarmTripRepository;
import com.farmily.trip.repository.FarmTripSessionRepository;
import com.farmily.trip.service.FarmTripService;
import com.farmily.trip.dto.OrderUpdateRequest;
import com.farmily.trip.model.FarmTripAudits;
import com.farmily.trip.model.AuditsStatus;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FarmTripServiceImpl implements FarmTripService {

	private final FarmTripRepository farmTripRepository;
	private final FarmTripSessionRepository farmTripSessionRepository;
	private final FarmTripAuditsRepository farmTripAuditsRepository;
	private final FarmTripOrderRepository farmTripOrderRepository;
	private final FarmTripCommentRepository farmTripCommentRepository;

	@Autowired
	public FarmTripServiceImpl(FarmTripRepository farmTripRepository,
			FarmTripSessionRepository farmTripSessionRepository, FarmTripAuditsRepository farmTripAuditsRepository,
			FarmTripOrderRepository farmTripOrderRepository, FarmTripCommentRepository farmTripCommentRepository) {
		this.farmTripRepository = farmTripRepository;
		this.farmTripSessionRepository = farmTripSessionRepository;
		this.farmTripAuditsRepository = farmTripAuditsRepository;
		this.farmTripOrderRepository = farmTripOrderRepository;
		this.farmTripCommentRepository = farmTripCommentRepository;
	}

	@Override
	public List<FarmTrip> getActiveTrips() {
		return farmTripRepository.findByTripStatus(TripStatus.ACTIVE);
	}

	// 回傳 DTO 版本的活動列表
	@Override
	public List<TripListResponse> getActiveTripList() {
		return farmTripRepository.findByTripStatus(TripStatus.ACTIVE).stream().map(this::toListResponse).toList();
	}

	@Override
	public TripDetailResponse getTripDetail(Integer farmTripId) {
		FarmTrip trip = farmTripRepository.findById(farmTripId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此活動"));

		if (trip.getTripStatus() != TripStatus.ACTIVE) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此活動");
		}

		TripDetailResponse dto = new TripDetailResponse();
		dto.setFarmTripId(trip.getFarmTripId());
		dto.setFarmTripTitle(trip.getFarmTripTitle());
		dto.setFarmTripType(trip.getFarmTripType().name());
		dto.setFarmTripIntro(trip.getFarmTripIntro());
		dto.setLocation(trip.getLocation());
		dto.setReferPrice(trip.getReferPrice());
		dto.setCommentNumbers(trip.getCommentNumbers());
		dto.setStarNumbers(trip.getStarNumbers());
		return dto;
	}

	private TripListResponse toListResponse(FarmTrip trip) {
		TripListResponse dto = new TripListResponse();
		dto.setFarmTripId(trip.getFarmTripId());
		dto.setFarmTripTitle(trip.getFarmTripTitle());
		dto.setFarmTripType(trip.getFarmTripType().name());
		dto.setLocation(trip.getLocation());
		dto.setReferPrice(trip.getReferPrice());
		dto.setStarNumbers(trip.getStarNumbers());
		return dto;
	}

	@Override
	public TripDetailResponse createTrip(TripCreateRequest request) {
		FarmTrip trip = new FarmTrip();
		trip.setFarmerId(request.getFarmerId());
		trip.setFarmTripType(FarmTripType.valueOf(request.getFarmTripType()));
		trip.setFarmTripTitle(request.getFarmTripTitle());
		trip.setFarmTripIntro(request.getFarmTripIntro());
		trip.setLocation(request.getLocation());
		trip.setReferPrice(request.getReferPrice());
		trip.setTripStatus(TripStatus.PENDING); // 核心規則：一律從「待審核」開始
		trip.setCommentNumbers(0);
		trip.setStarNumbers(0);

		// 【新增】有上傳圖片就存進 farmTripPic（byte[]）
		MultipartFile pic = request.getPic();
		if (pic != null && !pic.isEmpty()) {
			try {
				trip.setFarmTripPic(pic.getBytes());
			} catch (IOException e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "圖片讀取失敗");
			}
		}

		FarmTrip saved = farmTripRepository.save(trip);

		TripDetailResponse dto = new TripDetailResponse();
		dto.setFarmTripId(saved.getFarmTripId());
		dto.setFarmTripTitle(saved.getFarmTripTitle());
		dto.setFarmTripType(saved.getFarmTripType().name());
		dto.setFarmTripIntro(saved.getFarmTripIntro());
		dto.setLocation(saved.getLocation());
		dto.setReferPrice(saved.getReferPrice());
		dto.setCommentNumbers(saved.getCommentNumbers());
		dto.setStarNumbers(saved.getStarNumbers());
		return dto;
	}

	@Override
	@Transactional // 改活動狀態 + 寫審核紀錄，要同生共死
	public TripDetailResponse reviewTrip(Integer farmTripId, TripReviewRequest request) {
		FarmTrip trip = farmTripRepository.findById(farmTripId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此活動"));

		// 只有 PENDING 能被審核
		if (trip.getTripStatus() != TripStatus.PENDING) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "此活動目前狀態為 " + trip.getTripStatus() + "，不可審核");
		}

		boolean approved = Boolean.TRUE.equals(request.getApproved());
		if (approved) {
			trip.setTripStatus(TripStatus.ACTIVE);
		} else {
			trip.setTripStatus(TripStatus.REJECTED);
		}
		FarmTrip saved = farmTripRepository.save(trip);

		// 寫一筆審核稽核紀錄：誰審的、結果、意見、時間
		Timestamp now = new Timestamp(System.currentTimeMillis());
		FarmTripAudits audit = new FarmTripAudits();
		audit.setFarmTripId(farmTripId);
		audit.setAdminId(request.getAdminId());
		audit.setAuditsStatus(approved ? AuditsStatus.APPROVED : AuditsStatus.REJECTED);
		audit.setReason(request.getComment());
		audit.setCreatedAt(now);
		audit.setUpdatedAt(now);
		farmTripAuditsRepository.save(audit);

		TripDetailResponse dto = new TripDetailResponse();
		dto.setFarmTripId(saved.getFarmTripId());
		dto.setFarmTripTitle(saved.getFarmTripTitle());
		dto.setFarmTripType(saved.getFarmTripType().name());
		dto.setFarmTripIntro(saved.getFarmTripIntro());
		dto.setLocation(saved.getLocation());
		dto.setReferPrice(saved.getReferPrice());
		dto.setCommentNumbers(saved.getCommentNumbers());
		dto.setStarNumbers(saved.getStarNumbers());
		return dto;
	}

	@Override
	@Transactional
	public OrderResponse bookSession(Integer farmSessionId, OrderCreateRequest request) {
		FarmTripSession session = farmTripSessionRepository.findById(farmSessionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此場次"));

		if (session.getSessionStatus() != SessionStatus.ACTIVE) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "此場次不在報名狀態");
		}

		Timestamp now = new Timestamp(System.currentTimeMillis());
		if (session.getTripBookStart() != null && now.before(session.getTripBookStart())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "報名尚未開始");
		}
		if (session.getTripBookEnd() != null && now.after(session.getTripBookEnd())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "報名已截止");
		}

		if (request.getNumPeople() == null || request.getNumPeople() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "報名人數至少 1 人");
		}

		FarmTripOrder order = new FarmTripOrder();
		order.setFarmSessionId(farmSessionId);
		order.setUserId(request.getUserId());
		order.setNumPeople(request.getNumPeople());
		order.setUserName(request.getUserName());
		order.setUserPhoneNum(request.getUserPhoneNum());
		order.setNote(request.getNote());
		order.setOrderStatus(OrderStatus.CONFIRMED);
		order.setBookedAt(now);
		order.setFarmTripOrderBookingNo("FT" + System.currentTimeMillis());

		FarmTripOrder saved = farmTripOrderRepository.save(order);

		int attendance = session.getAttendance() == null ? 0 : session.getAttendance();
		session.setAttendance(attendance + request.getNumPeople());
		farmTripSessionRepository.save(session);

		return toOrderResponse(saved);
	}

	private OrderResponse toOrderResponse(FarmTripOrder order) {
		OrderResponse dto = new OrderResponse();
		dto.setFarmTripOrderId(order.getFarmTripOrderId());
		dto.setFarmSessionId(order.getFarmSessionId());
		dto.setFarmTripOrderBookingNo(order.getFarmTripOrderBookingNo());
		dto.setNumPeople(order.getNumPeople());
		dto.setOrderStatus(order.getOrderStatus().name());
		dto.setBookedAt(order.getBookedAt());
		dto.setUserName(order.getUserName());
		dto.setUserPhoneNum(order.getUserPhoneNum());
		dto.setNote(order.getNote());
		return dto;
	}

	@Override
	public List<OrderResponse> getMyOrders(Integer userId) {
		return farmTripOrderRepository.findByUserIdOrderByBookedAtDesc(userId).stream().map(this::toOrderResponse)
				.toList();
	}

	@Override
	@Transactional
	public OrderResponse cancelOrder(Integer farmTripOrderId) {
		FarmTripOrder order = farmTripOrderRepository.findById(farmTripOrderId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此訂單"));

		if (order.getOrderStatus() == OrderStatus.CANCELLED || order.getOrderStatus() == OrderStatus.COMPLETED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "此訂單目前狀態為 " + order.getOrderStatus() + "，不可取消");
		}

		order.setOrderStatus(OrderStatus.CANCELLED);
		order.setCancelledAt(new Timestamp(System.currentTimeMillis()));
		farmTripOrderRepository.save(order);

		FarmTripSession session = farmTripSessionRepository.findById(order.getFarmSessionId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此場次"));
		int attendance = session.getAttendance() == null ? 0 : session.getAttendance();
		int cancelled = order.getNumPeople() == null ? 0 : order.getNumPeople();
		session.setAttendance(Math.max(0, attendance - cancelled));
		farmTripSessionRepository.save(session);

		return toOrderResponse(order);
	}

	// ================= 場次 Session =================

	@Override
	public List<SessionResponse> getSessionsByTrip(Integer farmTripId) {
		return farmTripSessionRepository.findByFarmTripId(farmTripId).stream().map(this::toSessionResponse).toList();
	}

	@Override
	public SessionResponse createSession(Integer farmTripId, SessionCreateRequest request) {
		farmTripRepository.findById(farmTripId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此活動"));

		FarmTripSession session = new FarmTripSession();
		session.setFarmerTripId(farmTripId); // 注意：這個 entity 的 setter 就叫 setFarmerTripId
		session.setFarmTripStart(request.getFarmTripStart());
		session.setFarmTripEnd(request.getFarmTripEnd());
		session.setTripBookStart(request.getTripBookStart());
		session.setTripBookEnd(request.getTripBookEnd());
		session.setAttendance(0);
		session.setSessionStatus(SessionStatus.ACTIVE);

		FarmTripSession saved = farmTripSessionRepository.save(session);
		return toSessionResponse(saved);
	}

	private SessionResponse toSessionResponse(FarmTripSession session) {
		SessionResponse dto = new SessionResponse();
		dto.setFarmSessionId(session.getFarmSessionId());
		dto.setFarmTripId(session.getFarmerTripId()); // 注意：getter 就叫 getFarmerTripId
		dto.setFarmTripStart(session.getFarmTripStart());
		dto.setFarmTripEnd(session.getFarmTripEnd());
		dto.setTripBookStart(session.getTripBookStart());
		dto.setTripBookEnd(session.getTripBookEnd());
		dto.setAttendance(session.getAttendance());
		dto.setSessionStatus(session.getSessionStatus() == null ? null : session.getSessionStatus().name());
		return dto;
	}

	// ================= 評論 Comment =================

	@Override
	public List<CommentResponse> getComments(Integer farmTripId) {
		return farmTripCommentRepository.findByFarmTripIdOrderByCreatedAtDesc(farmTripId).stream()
				.map(this::toCommentResponse).toList();
	}

	@Override
	@Transactional
	public CommentResponse addComment(Integer farmTripId, CommentCreateRequest request) {
		FarmTrip trip = farmTripRepository.findById(farmTripId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此活動"));

		if (request.getStar() == null || request.getStar() < 1 || request.getStar() > 5) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "評分需為 1~5 星");
		}

		// 只有「參加過此活動」的會員才能評論：
		// 參加過 = 在此活動的任一場次，有一筆「未取消」的報名訂單
		List<Integer> sessionIds = farmTripSessionRepository.findByFarmTripId(farmTripId).stream()
				.map(FarmTripSession::getFarmSessionId).toList();
		boolean joined = farmTripOrderRepository.findByUserIdOrderByBookedAtDesc(request.getUserId()).stream().anyMatch(
				o -> sessionIds.contains(o.getFarmSessionId()) && o.getOrderStatus() != OrderStatus.CANCELLED);
		if (!joined) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有參加過此活動的會員才能評論");
		}

		FarmTripComment comment = new FarmTripComment();
		comment.setFarmTripId(farmTripId);
		comment.setUserId(request.getUserId());
		comment.setStar(request.getStar());
		comment.setReason(request.getContent()); // 留言內容存在 entity 的 reason 欄位
		comment.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		FarmTripComment saved = farmTripCommentRepository.save(comment);

		// 重新統計評論數與平均星數，寫回 FarmTrip
		List<FarmTripComment> all = farmTripCommentRepository.findByFarmTripIdOrderByCreatedAtDesc(farmTripId);
		int count = all.size();
		int sum = 0;
		for (FarmTripComment c : all) {
			sum += (c.getStar() == null ? 0 : c.getStar());
		}
		int avg = count == 0 ? 0 : Math.round((float) sum / count);
		trip.setCommentNumbers(count);
		trip.setStarNumbers(avg);
		farmTripRepository.save(trip);

		return toCommentResponse(saved);
	}

	private CommentResponse toCommentResponse(FarmTripComment comment) {
		CommentResponse dto = new CommentResponse();
		dto.setCommentId(comment.getFarmTripComment());
		dto.setFarmTripId(comment.getFarmTripId());
		dto.setUserId(comment.getUserId());
		dto.setStar(comment.getStar());
		dto.setContent(comment.getReason());
		dto.setCreatedAt(comment.getCreatedAt());
		return dto;
	}

	// ================= 會員修改預約 =================
	@Override
	@Transactional
	public OrderResponse updateOrder(Integer farmTripOrderId, OrderUpdateRequest request) {
		FarmTripOrder order = farmTripOrderRepository.findById(farmTripOrderId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此訂單"));

		// 只有「預約成功(CONFIRMED)」的訂單可以修改
		if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "此訂單目前狀態為 " + order.getOrderStatus() + "，不可修改");
		}

		// 若有改人數：先算差額，同步更新場次的已報名人數
		if (request.getNumPeople() != null) {
			if (request.getNumPeople() < 1) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "報名人數至少 1 人");
			}
			int oldPeople = order.getNumPeople() == null ? 0 : order.getNumPeople();
			int delta = request.getNumPeople() - oldPeople;
			if (delta != 0) {
				FarmTripSession session = farmTripSessionRepository.findById(order.getFarmSessionId())
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此場次"));
				int attendance = session.getAttendance() == null ? 0 : session.getAttendance();
				session.setAttendance(Math.max(0, attendance + delta));
				farmTripSessionRepository.save(session);
			}
			order.setNumPeople(request.getNumPeople());
		}

		// 聯絡資訊：有送才更新（null 就維持原值）
		if (request.getUserName() != null)
			order.setUserName(request.getUserName());
		if (request.getUserPhoneNum() != null)
			order.setUserPhoneNum(request.getUserPhoneNum());
		if (request.getNote() != null)
			order.setNote(request.getNote());

		FarmTripOrder saved = farmTripOrderRepository.save(order);
		return toOrderResponse(saved);
	}

	// ================= 小農前台 =================

	// 小農查自己發起的所有活動（含 PENDING / ACTIVE / REJECTED 各狀態）
	@Override
	public List<FarmTrip> getTripsByFarmer(Integer farmerId) {
		return farmTripRepository.findByFarmerId(farmerId);
	}

	// 小農查自己活動底下的所有報名訂單（repository 已寫好 JPQL：訂單→場次→活動→farmerId）
	@Override
	public List<OrderResponse> getFarmerOrders(Integer farmerId) {
		return farmTripOrderRepository.findOrdersByFarmerId(farmerId).stream().map(this::toOrderResponse).toList();
	}

	// 取活動圖片的位元組（給圖片端點用）
	@Override
	public byte[] getTripImage(Integer farmTripId) {
		FarmTrip trip = farmTripRepository.findById(farmTripId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此活動"));
		return trip.getFarmTripPic();
	}
}