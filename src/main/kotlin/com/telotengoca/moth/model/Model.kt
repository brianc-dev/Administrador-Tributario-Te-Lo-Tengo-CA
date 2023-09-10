package com.telotengoca.moth.model

import jakarta.persistence.criteria.CriteriaBuilder
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration

abstract class Model {
    companion object {

        val factory: SessionFactory = Configuration().configure().buildSessionFactory()

        @JvmStatic
        protected fun create(entity: Any) {
            factory.createEntityManager().use {em ->
                em.transaction.begin()
                em.persist(entity)
                em.transaction.commit()
            }
        }

        inline fun <reified T: Model> T.delete(id: Any): T {
            factory.createEntityManager().use {em ->
                val entity = em.find(T::class.java, id)
                em.remove(entity)
                return entity
            }
        }

        inline fun <reified T: Model> T.all(): List<T> {
            factory.openSession().use { session ->
                val builder: CriteriaBuilder = session.criteriaBuilder
                val criteria = builder.createQuery(T::class.java)
                val root = criteria.from(T::class.java)
                criteria.select(root)
                return session.createQuery(criteria).resultList
            }
        }

        inline fun<reified T: Model> T.get(id: Any): T? {
            factory.createEntityManager().use { em ->
                val query = em.createQuery("SELECT model FROM ${T::class.simpleName} AS model WHERE model.id=:id")
                query.setParameter("id", id)
                return query.singleResult as? T
            }
        }
    }
}