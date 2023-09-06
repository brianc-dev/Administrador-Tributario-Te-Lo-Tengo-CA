package com.telotengoca.moth.model

import jakarta.persistence.criteria.CriteriaBuilder
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration

abstract class Model {
    companion object {

        val factory: SessionFactory = Configuration().configure().buildSessionFactory()

        @JvmStatic
        protected fun create(entity: Any) {
            factory.openSession().use { session ->
                session.beginTransaction().also { transaction ->
                    session.persist(entity)
                }.commit()
            }
        }

        inline fun <reified T: Model> T.delete(id: Any): T {
            factory.createEntityManager().use {em ->
                val entity = em.find(T::class.java, id)
                em.remove(entity)
                return entity
            }
        }

        protected inline fun <reified T: Model> T.update(entity: T): T {
            factory.createEntityManager().use {em ->
                return em.merge(entity)
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

    protected fun <T : Model> T.save() {
        factory.openSession().use { session ->
            session.beginTransaction().also { transaction ->
                if (session.isDirty) {
                    session.merge(this)
                }
            }.commit()
        }
    }
}