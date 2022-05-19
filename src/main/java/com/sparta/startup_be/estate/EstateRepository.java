package com.sparta.startup_be.estate;

import com.sparta.startup_be.model.Estate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Map;


public interface EstateRepository extends JpaRepository<Estate, Long> {
//    List<Estate> findAllByFloor(int floor);
    List<Estate> findAllByCity(String city);
//    List<Estate> findAllByMonthly(String monthly);
    int countAllByMonthlyAndCity(String monthly, String city);
//     searchAllByCity(String city);
    @Query("select u from Estate u where u.city like :keyword")
    List<Estate> searchAllByCity(@Param("keyword")String city);
    @Query(nativeQuery = true,value = "select u.* from estate u where u.city like :keyword order by u.id limit 10 offset :start")
    List<Estate> searchAllByCityQuery(@Param("keyword")String city, @Param("start")int start);
    @Query(nativeQuery = true,value = "select u.* from estate u where u.dong like :keyword order by u.id limit 10 offset :start")
    List<Estate> searchAllByDongQuery(@Param("keyword")String city, @Param("start")int start);
    @Query(nativeQuery = true,value = "select u.* from estate u where u.gu like :keyword order by u.id limit 10 offset :start")
    List<Estate> searchAllByGuQuery(@Param("keyword")String city, @Param("start")int start);
    @Query("select count(u) from Estate u where u.city like :keyword")
    int countAllByCity(@Param("keyword")String city);

    @Query("select count(u) from Estate u where u.gu like :keyword")
    int countAllByGu(@Param("keyword")String gu);

    @Query("select count(u) from Estate u where u.dong like :keyword")
    int countAllByDong(@Param("keyword")String dong);
//    @Query(find)

//    @Query(nativeQuery = true,value = "select distinct e.city from estate e," +
//            " coordinate c  where e.id=c.estateid  and c.x between :minX and :maxX and c.y between :minY and :maxY")
//    List<String> findCity1(@Param("minX") float minX, @Param("maxX") float maxX, @Param("minY") float minY, @Param("maxY") float maxY);
//
//    @Query("select u from Estate u where u.city like %:keyword%")
//    List<Estate> searchAllByCity1(@Param("keyword")String city);
//    List<Estate> findAllByCity1(@Param("keyword")String city);

    @Query("select u from Estate u where u.dong like :keyword")
    List<Estate> searchAllBydong(@Param("keyword")String city);

    @Query("select avg (u.rent_fee) from Estate u where u.dong like :keyword group by u.dong")
    float dongAvgQuery(@Param("keyword")String dong);
    @Query("select avg (u.rent_fee) from Estate u where u.gu like :keyword group by u.gu")
    float guAvgQuery(@Param("keyword")String gu);
    @Query("select avg (u.rent_fee) from Estate u where u.city like :keyword group by u.city")
    float cityAvgQuery(@Param("keyword")String city);

    @Query("select avg (u.area) from Estate u where u.dong like :keyword group by u.dong")
    float dongAreaAvgQuery(@Param("keyword")String dong);
    @Query("select avg (u.area) from Estate u where u.gu like :keyword group by u.gu")
    float guAvgAreaQuery(@Param("keyword")String gu);
    @Query("select avg (u.area) from Estate u where u.city  like :keyword group by u.city")
    float cityAreaAvgQuery(@Param("keyword")String city);

    @Query("select u from Estate u where u.gu like :keyword")
    List<Estate> searchAllBygu(@Param("keyword")String city);


    @Query(nativeQuery = true,value = "select distinct e.city from estate e," +
            " coordinate c  where e.id=c.estateid  and c.x between :minX and :maxX and c.y between :minY and :maxY")
    List<String> findCity(@Param("minX") float minX, @Param("maxX") float maxX, @Param("minY") float minY, @Param("maxY") float maxY);

    @Query(nativeQuery = true,value = "select distinct e.gu from estate e," +
            " coordinate c  where e.id=c.estateid  and c.x between :minX and :maxX and c.y between :minY and :maxY")
    List<String> findGu(@Param("minX") float minX, @Param("maxX") float maxX, @Param("minY") float minY, @Param("maxY") float maxY);

    @Query(nativeQuery = true,value = "select distinct e.dong from estate e," +
            " coordinate c  where e.id=c.estateid  and c.x between :minX and :maxX and c.y between :minY and :maxY")
    List<String> findDong(@Param("minX") float minX, @Param("maxX") float maxX, @Param("minY") float minY, @Param("maxY") float maxY);
}