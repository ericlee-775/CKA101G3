package com.farmily.trip.service.impl;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.farmily.trip.dto.CommentCreateRequest;
import com.farmily.trip.dto.CommentResponse;
import com.farmily.trip.dto.OrderCreateRequest;
import com.farmily.trip.dto.OrderResponse;
import com.farmily.trip.dto.OrderUpdateRequest;
import com.farmily.trip.dto.SessionCreateRequest;
import com.farmily.trip.dto.SessionResponse;
import com.farmily.trip.dto.TripCreateRequest;
import com.farmily.trip.dto.TripDetailResponse;
import com.farmily.trip.dto.TripListResponse;
import com.farmily.trip.dto.TripReviewRequest;
import com.farmily.trip.model.AuditsStatus;
import com.farmily.trip.model.FarmTrip;
import com.farmily.trip.model.FarmTripAudits;
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
import com.farmily.user.repository.FarmerRepository;
import com.farmily.user.repository.UserRepository;
import com.farmily.user.service.EmailService;
import com.farmily.trip.dto.SessionNotifyRequest;
import com.farmily.trip.dto.TripUpdateRequest;
import com.farmily.user.model.User;

import com.farmily.notification.service.NotificationSender;

@Service
public class FarmTripServiceImpl implements FarmTripService {

	private final FarmTripRepository farmTripRepository;
	private final FarmTripSessionRepository farmTripSessionRepository;
	private final FarmTripAuditsRepository farmTripAuditsRepository;
	private final FarmTripOrderRepository farmTripOrderRepository;
	private final FarmTripCommentRepository farmTripCommentRepository;
	private final FarmerRepository farmerRepository;
	private final UserRepository userRepository;
	private final EmailService emailService;
	private final NotificationSender notificationSender;
	
	@Autowired
	public FarmTripServiceImpl(FarmTripRepository farmTripRepository,
			FarmTripSessionRepository farmTripSessionRepository, FarmTripAuditsRepository farmTripAuditsRepository,
			FarmTripOrderRepository farmTripOrderRepository, FarmTripCommentRepository farmTripCommentRepository,
			FarmerRepository farmerRepository, UserRepository userRepository, EmailService emailService,
			NotificationSender notificationSender) {
		
		this.farmTripRepository = farmTripRepository;
		this.farmTripSessionRepository = farmTripSessionRepository;
		this.farmTripAuditsRepository = farmTripAuditsRepository;
		this.farmTripOrderRepository = farmTripOrderRepository;
		this.farmTripCommentRepository = farmTripCommentRepository;
		this.farmerRepository = farmerRepository;
		this.userRepository = userRepository;
		this.emailService = emailService;
		this.notificationSender = notificationSender;
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

		String farmName = farmerRepository.findById(trip.getFarmerId()).map(f -> f.getFarmName()).orElse(null);
		dto.setFarmName(farmName);

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

		String farmName = farmerRepository.findById(trip.getFarmerId()).map(f -> f.getFarmName()).orElse(null);
		dto.setFarmName(farmName);
		dto.setFarmerId(trip.getFarmerId());

		dto.setRegistrationStatus(computeRegistrationStatus(trip.getFarmTripId()));

		return dto;
	}

	// 依場次計算活動的報名狀態：
	// 有任一可報名場次 → OPEN；否則有已截止場次 → CLOSED；否則 → CANCELLED；無場次 → null
	private String computeRegistrationStatus(Integer farmTripId) {
		List<FarmTripSession> sessions = farmTripSessionRepository.findByFarmTripId(farmTripId);
		if (sessions.isEmpty())
			return null;
		Timestamp now = new Timestamp(System.currentTimeMillis());
		boolean anyOpen = false, anyClosed = false;
		for (FarmTripSession s : sessions) {
			if (s.getSessionStatus() == SessionStatus.CANCELLED) {
				continue;
			}
			if (s.getTripBookEnd() != null && s.getTripBookEnd().before(now)) {
				anyClosed = true;
			} else {
				anyOpen = true;
			}
		}
		if (anyOpen)
			return "OPEN";
		if (anyClosed)
			return "CLOSED";
		return "CANCELLED";
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

		Integer bookFarmerId = farmTripRepository.findById(session.getFarmerTripId())
				.map(FarmTrip::getFarmerId).orElse(null);
		try {
			notificationSender.sendTripBookConfirmed(saved.getUserId(), bookFarmerId, saved.getFarmSessionId(), saved.getFarmTripOrderId());
		} catch (Exception e) {
			System.out.println("[通知] 報名通知失敗：" + e.getMessage());
		}
		
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

		farmTripSessionRepository.findById(order.getFarmSessionId()).ifPresent(session -> {
			dto.setFarmTripEnd(session.getFarmTripEnd());
			dto.setFarmTripStart(session.getFarmTripStart());
			Integer tripId = session.getFarmerTripId();
			dto.setFarmTripId(tripId);
			farmTripRepository.findById(tripId).ifPresent(t -> dto.setFarmTripTitle(t.getFarmTripTitle()));
		});

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

		FarmTrip cancelTrip = farmTripRepository.findById(session.getFarmerTripId()).orElse(null);
		Integer cancelFarmerId = cancelTrip == null ? null : cancelTrip.getFarmerId();
		String cancelTitle = cancelTrip == null ? "" : cancelTrip.getFarmTripTitle();
		try {
			notificationSender.sendTripBookCancelled(order.getUserId(), cancelFarmerId, order.getFarmTripOrderId(), cancelTitle);
		} catch (Exception e) {
			System.out.println("[通知] 取消預約通知失敗：" + e.getMessage());
		}
		
		return toOrderResponse(order);
	}

	// ================= 場次 Session =================

	// 小農修改場次時間（已取消的場次不可再改）
	@Override
	@Transactional
	public SessionResponse updateSession(Integer farmSessionId, SessionCreateRequest request) {
		FarmTripSession session = farmTripSessionRepository.findById(farmSessionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此場次"));

		if (session.getSessionStatus() == SessionStatus.CANCELLED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "此場次已取消，無法修改");
		}

		session.setFarmTripStart(request.getFarmTripStart());
		session.setFarmTripEnd(request.getFarmTripEnd());
		session.setTripBookStart(request.getTripBookStart());
		session.setTripBookEnd(request.getTripBookEnd());

		FarmTripSession saved = farmTripSessionRepository.save(session);
		return toSessionResponse(saved);
	}

	// 小農取消場次：改場次狀態為 CANCELLED，並連帶取消該場次「未取消」的報名、email 通知報名者
	@Override
	@Transactional
	public SessionResponse cancelSession(Integer farmSessionId) {
		FarmTripSession session = farmTripSessionRepository.findById(farmSessionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此場次"));

		if (session.getSessionStatus() == SessionStatus.CANCELLED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "此場次已經是取消狀態");
		}

		String tripTitle = farmTripRepository.findById(session.getFarmerTripId()).map(FarmTrip::getFarmTripTitle)
				.orElse("體驗活動");
		String sessionTime = session.getFarmTripStart() == null ? "" : session.getFarmTripStart().toString();

		List<FarmTripOrder> orders = farmTripOrderRepository.findByFarmSessionId(farmSessionId);

		java.util.Set<Integer> affectedUserIds = new java.util.HashSet<>();   // ★新增①：準備一個集合裝被取消的報名者
		Integer anyOrderId = null;                                            // ★新增②：記一筆訂單 id 給通知用

		for (FarmTripOrder order : orders) {
			if (order.getOrderStatus() == OrderStatus.CANCELLED) {
				continue;
			}
			order.setOrderStatus(OrderStatus.CANCELLED);
			order.setCancelledAt(new Timestamp(System.currentTimeMillis()));
			farmTripOrderRepository.save(order);

			if (order.getUserId() != null) {                                  // ★新增③：把這個報名者收集起來
				affectedUserIds.add(order.getUserId());
				anyOrderId = order.getFarmTripOrderId();
			}

			userRepository.findById(order.getUserId()).ifPresent(user -> {
				if (user.getEmail() != null && !user.getEmail().isBlank()) {
					emailService.sendTripSessionCancelledEmail(user.getEmail(), order.getUserName(), tripTitle, sessionTime);
				}
			});
		}

		if (!affectedUserIds.isEmpty()) {                                     // ★新增④：迴圈跑完後，一次通知所有被取消的報名者
			try {
				notificationSender.sendTripCancelled(affectedUserIds, farmSessionId, anyOrderId);
			} catch (Exception e) {
				System.out.println("[通知] 場次取消通知失敗：" + e.getMessage());
			}
		}

		session.setAttendance(0);
		session.setSessionStatus(SessionStatus.CANCELLED);
		FarmTripSession saved = farmTripSessionRepository.save(session);
		return toSessionResponse(saved);
	}

	// 小農主動寄提醒信給該場次「未取消」的報名者（主旨/內文自訂），回傳實際寄出人數
	@Override
	public int notifySessionRegistrants(Integer farmSessionId, SessionNotifyRequest request) {
		FarmTripSession session = farmTripSessionRepository.findById(farmSessionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此場次"));

		if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "請填寫通知內容");
		}

		String tripTitle = farmTripRepository.findById(session.getFarmerTripId()).map(FarmTrip::getFarmTripTitle)
				.orElse("體驗活動");
		String sessionTime = session.getFarmTripStart() == null ? "" : session.getFarmTripStart().toString();

		java.util.Set<Integer> sentUserIds = new java.util.HashSet<>();
		int sent = 0;
		for (FarmTripOrder order : farmTripOrderRepository.findByFarmSessionId(farmSessionId)) {
			if (order.getOrderStatus() != OrderStatus.CONFIRMED) {
				continue;
			}
			if (order.getUserId() == null || !sentUserIds.add(order.getUserId())) {
				continue;
			}
			User user = userRepository.findById(order.getUserId()).orElse(null);
			if (user != null && user.getEmail() != null && !user.getEmail().isBlank()) {
				emailService.sendTripReminderEmail(user.getEmail(), order.getUserName(), tripTitle, sessionTime,
						request.getSubject(), request.getMessage());
				sent++;
			}
		}
		return sent;
	}

	// 小農修改自己的活動基本資料；修改後狀態一律改回 PENDING，需管理員重新審核
	@Override
	@Transactional
	public TripDetailResponse updateTrip(Integer farmTripId, TripUpdateRequest request) {
		FarmTrip trip = farmTripRepository.findById(farmTripId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此活動"));

		if (request.getFarmerId() == null || !request.getFarmerId().equals(trip.getFarmerId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "無權修改此活動");
		}

		if (request.getFarmTripTitle() == null || request.getFarmTripTitle().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "活動標題不可為空");
		}

		if (request.getFarmTripType() != null) {
			trip.setFarmTripType(FarmTripType.valueOf(request.getFarmTripType()));
		}
		trip.setFarmTripTitle(request.getFarmTripTitle());
		trip.setFarmTripIntro(request.getFarmTripIntro());
		trip.setLocation(request.getLocation());
		trip.setReferPrice(request.getReferPrice());

		MultipartFile pic = request.getPic();
		if (pic != null && !pic.isEmpty()) {
			try {
				trip.setFarmTripPic(pic.getBytes());
			} catch (IOException e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "圖片讀取失敗");
			}
		}

		trip.setTripStatus(TripStatus.PENDING);

		FarmTrip saved = farmTripRepository.save(trip);

		TripDetailResponse dto = new TripDetailResponse();
		dto.setFarmTripId(saved.getFarmTripId());
		dto.setFarmTripTitle(saved.getFarmTripTitle());
		dto.setFarmTripType(saved.getFarmTripType() == null ? null : saved.getFarmTripType().name());
		dto.setFarmTripIntro(saved.getFarmTripIntro());
		dto.setLocation(saved.getLocation());
		dto.setReferPrice(saved.getReferPrice());
		dto.setCommentNumbers(saved.getCommentNumbers());
		dto.setStarNumbers(saved.getStarNumbers());
		return dto;
	}

	// 小農刪除自己的活動；只要任一場次有「未取消」的報名就禁止刪除
	@Override
	@Transactional
	public void deleteTrip(Integer farmTripId, Integer farmerId) {
		FarmTrip trip = farmTripRepository.findById(farmTripId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此活動"));

		if (farmerId == null || !farmerId.equals(trip.getFarmerId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "無權刪除此活動");
		}

		List<FarmTripSession> sessions = farmTripSessionRepository.findByFarmTripId(farmTripId);

		boolean hasRegistration = sessions.stream()
				.flatMap(s -> farmTripOrderRepository.findByFarmSessionId(s.getFarmSessionId()).stream())
				.anyMatch(o -> o.getOrderStatus() != OrderStatus.CANCELLED);
		if (hasRegistration) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "此活動已有人報名，無法刪除。如需停止招募，請改用「取消場次」。");
		}

		for (FarmTripSession s : sessions) {
			List<FarmTripOrder> sessionOrders = farmTripOrderRepository.findByFarmSessionId(s.getFarmSessionId());
			if (!sessionOrders.isEmpty()) {
				farmTripOrderRepository.deleteAll(sessionOrders);
			}
		}

		if (!sessions.isEmpty()) {
			farmTripSessionRepository.deleteAll(sessions);
		}

		List<FarmTripAudits> audits = farmTripAuditsRepository.findByFarmTripIdOrderByCreatedAtDesc(farmTripId);
		if (!audits.isEmpty()) {
			farmTripAuditsRepository.deleteAll(audits);
		}

		List<FarmTripComment> comments = farmTripCommentRepository.findByFarmTripIdOrderByCreatedAtDesc(farmTripId);
		if (!comments.isEmpty()) {
			farmTripCommentRepository.deleteAll(comments);
		}

		farmTripRepository.delete(trip);
	}

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

		// 只有「報名且參加完畢」的會員才能評論：
		// 參加完畢 = 在此活動「已結束(活動結束時間 < 現在)」的場次，有一筆未取消的報名
		Timestamp nowTs = new Timestamp(System.currentTimeMillis());
		List<Integer> endedSessionIds = farmTripSessionRepository.findByFarmTripId(farmTripId).stream()
				.filter(s -> s.getFarmTripEnd() != null && s.getFarmTripEnd().before(nowTs))
				.map(FarmTripSession::getFarmSessionId).toList();
		boolean completed = farmTripOrderRepository.findByUserIdOrderByBookedAtDesc(request.getUserId()).stream()
				.anyMatch(o -> endedSessionIds.contains(o.getFarmSessionId())
						&& o.getOrderStatus() != OrderStatus.CANCELLED);
		if (!completed) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有報名並參加完畢（活動已結束）的會員才能評論");
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

		try {
			notificationSender.sendTripComment(trip.getFarmerId(), farmTripId);
		} catch (Exception e) {
			System.out.println("[通知] 評論通知失敗：" + e.getMessage());
		}
		
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

		String email = userRepository.findById(comment.getUserId()).map(User::getEmail).orElse(null);
		dto.setMaskedAccount(maskEmail(email));

		return dto;
	}

	// 把 email 遮罩成「前 2 碼 + XXX + @網域」，例如 user1@gmail.com → usXXX@gmail.com
	private String maskEmail(String email) {
		if (email == null || email.isBlank())
			return "匿名會員";
		int at = email.indexOf('@');
		if (at <= 0)
			return "匿名會員";
		String local = email.substring(0, at);
		String domain = email.substring(at); // 含 @
		String head = local.length() >= 2 ? local.substring(0, 2) : local;
		return head + "XXX" + domain;
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

	// 管理後台：所有 PENDING（待審核）的活動
	@Override
	public List<FarmTrip> getPendingTrips() {
		return farmTripRepository.findByTripStatus(TripStatus.PENDING);
	}

	@Override
	public List<TripListResponse> getActiveTripListByFarmer(Integer farmerId) {
		return farmTripRepository.findByFarmerId(farmerId).stream().filter(t -> t.getTripStatus() == TripStatus.ACTIVE)
				.map(this::toListResponse).toList();
	}

	// 小農重啟已取消的場次：狀態改回 ACTIVE 開放重新報名
	// （之前取消時的舊報名維持已取消、不自動恢復；報名人數歸零）
	@Override
	@Transactional
	public SessionResponse reopenSession(Integer farmSessionId) {
		FarmTripSession session = farmTripSessionRepository.findById(farmSessionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此場次"));

		if (session.getSessionStatus() != SessionStatus.CANCELLED) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "只有『已取消』的場次才能重啟");
		}

		session.setSessionStatus(SessionStatus.ACTIVE);
		session.setAttendance(0);
		FarmTripSession saved = farmTripSessionRepository.save(session);
		return toSessionResponse(saved);
	}
}