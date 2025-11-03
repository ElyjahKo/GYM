package com.gymapp.app.data;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.time.LocalDate;

public class MemberService {
    public List<Member> listAll() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Member order by id desc", Member.class).getResultList();
        }
    }

    public List<Member> searchByName(String q) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Member m where lower(m.fullName) like :q order by m.fullName", Member.class)
                    .setParameter("q", "%" + q.toLowerCase() + "%")
                    .getResultList();
        }
    }

    public Member findByQr(String qr) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Member m where m.qrCode = :qr", Member.class)
                    .setParameter("qr", qr)
                    .uniqueResult();
        }
    }

    public Member save(Member m) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            s.persist(m);
            tx.commit();
            return m;
        }
    }

    public Member update(Member m) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            s.merge(m);
            tx.commit();
            return m;
        }
    }

    public void delete(Member m) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            Member attached = s.get(Member.class, m.getId());
            if (attached != null) s.remove(attached);
            tx.commit();
        }
    }

    public Member subscribeInitial(Member m, int months) {
        m.setSubscriptionStatus(Member.SubscriptionStatus.ACTIVE);
        m.setSubscriptionEndAt(LocalDate.now().plusMonths(months));
        return update(m.getId() == null ? save(m) : m);
    }

    public Member renew(Member m, int months) {
        LocalDate base = m.getSubscriptionEndAt() != null && m.getSubscriptionEndAt().isAfter(LocalDate.now())
                ? m.getSubscriptionEndAt() : LocalDate.now();
        m.setSubscriptionStatus(Member.SubscriptionStatus.ACTIVE);
        m.setSubscriptionEndAt(base.plusMonths(months));
        return update(m);
    }
}
