package com.gymapp.app.data;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;

public class SubscriptionService {
    public List<Subscription> listByMember(Member member) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Subscription s where s.member.id = :mid order by s.startAt desc", Subscription.class)
                    .setParameter("mid", member.getId())
                    .getResultList();
        }
    }

    public Subscription createInitial(Member m, String plan, int quantityMonths, int priceFcfa, LocalDate startAt) {
        LocalDate start = startAt != null ? startAt : LocalDate.now();
        LocalDate end = start.plusMonths(quantityMonths);
        Subscription sub = new Subscription();
        sub.setMember(m);
        sub.setType(plan); // legacy
        sub.setPlan(plan);
        sub.setQuantityMonths(quantityMonths);
        sub.setPrice(priceFcfa);
        sub.setStatus(Subscription.Status.ACTIVE);
        sub.setStartAt(start);
        sub.setEndAt(end);
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            s.persist(sub);
            // reflect on member
            m.setSubscriptionStatus(Member.SubscriptionStatus.ACTIVE);
            m.setSubscriptionEndAt(end);
            s.merge(m);
            tx.commit();
        }
        return sub;
    }

    public Subscription renew(Member m, int months, int priceFcfa, String plan) {
        LocalDate base = m.getSubscriptionEndAt() != null && m.getSubscriptionEndAt().isAfter(LocalDate.now())
                ? m.getSubscriptionEndAt() : LocalDate.now();
        LocalDate newEnd = base.plusMonths(months);
        Subscription sub = new Subscription();
        sub.setMember(m);
        sub.setType(plan == null ? "Renouvellement" : plan);
        sub.setPlan(plan == null ? "Renouvellement" : plan);
        sub.setQuantityMonths(months);
        sub.setPrice(priceFcfa);
        sub.setStatus(Subscription.Status.ACTIVE);
        sub.setStartAt(base);
        sub.setEndAt(newEnd);
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            s.persist(sub);
            // reflect on member
            m.setSubscriptionStatus(Member.SubscriptionStatus.ACTIVE);
            m.setSubscriptionEndAt(newEnd);
            s.merge(m);
            tx.commit();
        }
        return sub;
    }

    public List<Subscription> listPaged(int page, int pageSize, String statusFilter, String nameQuery) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "select s from Subscription s join s.member m where 1=1";
            if (nameQuery != null && !nameQuery.isBlank()) hql += " and lower(m.fullName) like :q";
            if (statusFilter != null) {
                if (statusFilter.equalsIgnoreCase("active")) {
                    hql += " and s.endAt >= current_date";
                } else if (statusFilter.equalsIgnoreCase("expired")) {
                    hql += " and s.endAt < current_date";
                }
            }
            hql += " order by s.startAt desc";
            var query = s.createQuery(hql, Subscription.class)
                    .setFirstResult(Math.max(0, (page - 1)) * pageSize)
                    .setMaxResults(pageSize);
            if (nameQuery != null && !nameQuery.isBlank()) query.setParameter("q", "%" + nameQuery.toLowerCase() + "%");
            return query.getResultList();
        }
    }
}
