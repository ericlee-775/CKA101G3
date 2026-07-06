package com.farmily.trip.service.impl;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.farmily.trip.dto.OrderCreateRequest;
import com.farmily.trip.dto.OrderResponse;
import com.farmily.trip.dto.TripCreateRequest;
import com.farmily.trip.dto.TripDetailResponse;
import com.farmily.trip.dto.TripListResponse;
import com.farmily.trip.dto.TripReviewRequest;
import com.farmily.trip.model.FarmTrip;
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

@Service
public class FarmTripServiceImpl implements FarmTripService {

	private final FarmTripRepository farmTripRepository;
	private final FarmTripSessionRepository farmTripSessionRepository;
	private final FarmTripAuditsRepository farmTripAuditsRepository;
	private final FarmTripOrderRepository farmTripOrderRepository;
	private final FarmTripCommentRepository farmTripCommentRepository;

	@Autowired
	public FarmTripServiceImpl(FarmTripRepository farmTripRepository,
			FarmTripSessionRepository farmTripSessionRepository,
			FarmTripAuditsRepository farmTripAuditsRepository,
			FarmTripOrderRepository farmTripOrderRepository,
			FarmTripCommentRepository farmTripCommentRepository
			) {
		this.farmTripRepository = farmTripRepository;
		this.farmTripSessionRepository = farmTripSessionRepository;
		this.farmTripAuditsRepository = farmTripAuditsRepository;
		this.farmTripOrderRepository = farmTripOrderRepository;
		this.farmTripCommentRepository = farmTripCommentRepository;
	}

	// 你同學原本的方法，保留不動
	@Override
	public List<FarmTrip> getActiveTrips() {
		return farmTripRepository.findByTripStatus(TripStatus.ACTIVE);
	}

	// 新方法：回傳 DTO 版本的活動列表
	@Override
	public List<TripListResponse> getActiveTripList() {
		return farmTripRepository.findByTripStatus(TripStatus.ACTIVE)
				.stream()
				.map(this::toListResponse)
				.toList();
	}
	
	@Override
	public TripDetailResponse getTripDetail(Integer farmTripId) {
		// 用 id 去資料庫找，找不到就丟出 404
		FarmTrip trip = farmTripRepository.findById(farmTripId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此活動"));

		// 不是上架中的活動，消費者不該看到，一樣回 404
		if (trip.getTripStatus() != TripStatus.ACTIVE) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此活動");
		}

		// Entity 轉 DTO（跟早上的 toListResponse 同一個套路，只是欄位多一點）
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

	// Entity 轉 DTO 的搬運工
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
		// 這次方向相反：把 DTO 的資料搬進一個「新的」Entity
		FarmTrip trip = new FarmTrip();
		trip.setFarmerId(request.getFarmerId());
		trip.setFarmTripType(FarmTripType.valueOf(request.getFarmTripType())); // 字串轉 enum
		trip.setFarmTripTitle(request.getFarmTripTitle());
		trip.setFarmTripIntro(request.getFarmTripIntro());
		trip.setLocation(request.getLocation());
		trip.setReferPrice(request.getReferPrice());

		trip.setTripStatus(TripStatus.PENDING);  // 核心規則：一律從「待審核」開始
		trip.setCommentNumbers(0);                // 新活動評論數、星數從 0 起算
		trip.setStarNumbers(0);

		// save() 會執行 INSERT，回傳「帶著資料庫給的新 id」的 Entity
		FarmTrip saved = farmTripRepository.save(trip);

		// 轉成 DTO 回傳（getTripDetail 裡寫過一次了，這裡再搬一次）
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
	public TripDetailResponse reviewTrip(Integer farmTripId, TripReviewRequest request) {
		// 找不到就 404
		FarmTrip trip = farmTripRepository.findById(farmTripId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此活動"));

		// 狀態機規則：只有 PENDING 能被審核
		if (trip.getTripStatus() != TripStatus.PENDING) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"此活動目前狀態為 " + trip.getTripStatus() + "，不可審核");
		}

		// 依審核結果轉換狀態
		if (Boolean.TRUE.equals(request.getApproved())) {
			trip.setTripStatus(TripStatus.ACTIVE);
		} else {
			trip.setTripStatus(TripStatus.REJECTED);
		}

		FarmTrip saved = farmTripRepository.save(trip);  // 這次 save 是 UPDATE，不是 INSERT

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
	@Transactional  // 建訂單＋更新人數要同生共死
	public OrderResponse bookSession(Integer farmSessionId, OrderCreateRequest request) {
		// 1. 找場次
		FarmTripSession session = farmTripSessionRepository.findById(farmSessionId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此場次"));

		// 2. 場次要在報名中
		if (session.getSessionStatus() != SessionStatus.ACTIVE) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "此場次不在報名狀態");
		}

		// 3. 現在時間要落在報名期間內
		Timestamp now = new Timestamp(System.currentTimeMillis());
		if (session.getTripBookStart() != null && now.before(session.getTripBookStart())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "報名尚未開始");
		}
		if (session.getTripBookEnd() != null && now.after(session.getTripBookEnd())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "報名已截止");
		}

		// 4. 報名人數要合理
		if (request.getNumPeople() == null || request.getNumPeople() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "報名人數至少 1 人");
		}

		// 5. 建立訂單
		FarmTripOrder order = new FarmTripOrder();
		order.setFarmSessionId(farmSessionId);
		order.setUserId(request.getUserId());
		order.setNumPeople(request.getNumPeople());
		order.setUserName(request.getUserName());
		order.setUserPhoneNum(request.getUserPhoneNum());
		order.setNote(request.getNote());
		order.setOrderStatus(OrderStatus.CONFIRMED);
		order.setBookedAt(now);
		// 訂單編號：FT + 時間戳，簡單且幾乎不重複
		order.setFarmTripOrderBookingNo("FT" + System.currentTimeMillis());

		FarmTripOrder saved = farmTripOrderRepository.save(order);

		// 6. 累加場次的已報名人數
		int attendance = session.getAttendance() == null ? 0 : session.getAttendance();
		session.setAttendance(attendance + request.getNumPeople());
		farmTripSessionRepository.save(session);

		return toOrderResponse(saved);
	}

	// Entity 轉 DTO：報名和之後的「我的報名」共用
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
	
	// 我的報名：用 userId 撈訂單，轉成 DTO 清單
		@Override
		public List<OrderResponse> getMyOrders(Integer userId) {
			return farmTripOrderRepository.findByUserIdOrderByBookedAtDesc(userId)
					.stream()
					.map(this::toOrderResponse)   // 報名時寫的轉換方法，這裡重用
					.toList();
		}

		// 取消報名（同時把人數扣回場次）
		@Override
		@Transactional
		public OrderResponse cancelOrder(Integer farmTripOrderId) {
			FarmTripOrder order = farmTripOrderRepository.findById(farmTripOrderId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此訂單"));

			// 狀態機：已取消或已完成的訂單不能再取消
			if (order.getOrderStatus() == OrderStatus.CANCELLED
					|| order.getOrderStatus() == OrderStatus.COMPLETED) {
				throw new ResponseStatusException(HttpStatus.CONFLICT,
						"此訂單目前狀態為 " + order.getOrderStatus() + "，不可取消");
			}

			// 1. 改訂單狀態
			order.setOrderStatus(OrderStatus.CANCELLED);
			order.setCancelledAt(new Timestamp(System.currentTimeMillis()));
			farmTripOrderRepository.save(order);

			// 2. 把這筆訂單的人數從場次的 attendance 扣回去
			FarmTripSession session = farmTripSessionRepository.findById(order.getFarmSessionId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此場次"));
			int attendance = session.getAttendance() == null ? 0 : session.getAttendance();
			int cancelled = order.getNumPeople() == null ? 0 : order.getNumPeople();
			session.setAttendance(Math.max(0, attendance - cancelled));  // 用 max 防呆，避免扣成負數
			farmTripSessionRepository.save(session);

			return toOrderResponse(order);
		}

}