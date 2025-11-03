package com.gymapp.app.data;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class AttendanceService {
    private final MemberService memberService = new MemberService();

    public Attendance checkInByCode(String code) {
        if (code == null || code.isBlank()) return null;
        Member m = memberService.findByQr(code.trim());
        if (m == null) return null;
        Attendance a = new Attendance();
        a.setMember(m);
        a.setCheckedAt(LocalDateTime.now());
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            s.persist(a);
            tx.commit();
        }
        return a;
    }

    public List<Attendance> latest(int limit) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Attendance a order by a.checkedAt desc", Attendance.class)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }
}
