
package com.farmily.groupbuy.model;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface GroupBuyRepository extends JpaRepository<GroupBuyVO, Integer> {

//	@Transactional
//	@Modifying
//	@Query(value = "delete from emp3 where empno =?1", nativeQuery = true)
//	void deleteByEmpno(int empno);
//	
	List<GroupBuyVO> findByRequestStatus(RequestStatus requestStatus,GroupBuyStatus groupBuyStatus);
//	
//	//● (自訂)條件查詢
//	@Query(value = "from EmpVO where empno=?1 and ename like?2 and hiredate=?3 order by empno")
//	List<GroupBuyVO> findByOthers(int empno , String ename , java.sql.Date hiredate);
}