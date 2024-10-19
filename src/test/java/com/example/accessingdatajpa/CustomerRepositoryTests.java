/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.accessingdatajpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Selection;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.jar.asm.Opcodes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
public class CustomerRepositoryTests {
	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private CustomerRepository customers;

    @Test
	public void testFindByLastName() throws ClassNotFoundException {
		Map<String,Class<?>> map = new HashMap<>();
		var entities = entityManager.getEntityManager().getMetamodel().getEntities();
		for (var entity : entities) {
			map.put(entity.getName(), entity.getClass());
		}

//		.stream().collect(Collectors.toMap(Entity::getKey, item -> item));


		Customer customer = new Customer("first", "last");
		entityManager.persist(customer);

		List<Customer> findByLastName = customers.findByLastName(customer.getLastName());


		 var query = entityManager.getEntityManager().createQuery("select firstName from Customer  where lastName = :lastName");
		 query.setParameter("lastName", "last");
		 List<String> result = query.getResultList();

		var cb = entityManager.getEntityManager().getCriteriaBuilder().createQuery(map.get("Customer"));
		Selection<?> select;
//		cb.select(select);

//		Specification<> ageLessThan18 = (root, query, cb) -> cb.lessThan(root.get("age").as(Integer.class), 18)

		assertThat(findByLastName).extracting(Customer::getLastName).containsOnly(customer.getLastName());
	}

	@Test
	public void dynamicCriteria() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InstantiationException {
		CriteriaBuilder builder = entityManager.getEntityManager().getCriteriaBuilder();

		Map<String,Class<?>> map = new HashMap<>();
		var entities = entityManager.getEntityManager().getMetamodel().getEntities();
		for (var entity : entities) {
			map.put(entity.getName(), entity.getJavaType());
		}

		var cq = builder.createQuery(String.class);
		var student = cq.from(map.get("Customer"));
		cq.select(student.get("lastName"));
		Predicate[] predicates = new Predicate[]{};
		cq.where(predicates);
		var query = entityManager.getEntityManager().createQuery(cq);
		query.setFirstResult(1);
		query.setMaxResults(2);

		var studentResult = query.getResultList();
		System.out.println(studentResult);


//		Specification condition = (root, query, cb) -> cb.equal(root.get("lastName"), "Dessler");
//
//		var result = customers.findAll(condition);
//		System.out.println(result);
	}


	public Class<?> createClass(String fieldName) throws NoSuchFieldException, IllegalAccessException, InstantiationException {
		Class<?> target = new ByteBuddy()
				.subclass(Object.class)
				.name("com.taomish.Dto")
				.defineField("fieldName",String.class, Opcodes.ACC_PUBLIC)
				.make()
				.load(ClassLoader.getSystemClassLoader()).getLoaded();

		return target.getDeclaringClass();
//		Object targetObj = target.newInstance();
//
//		Field f = target.getDeclaredField("hello");
//		f.setAccessible(true);
//		System.out.println(f.get(targetObj));

	}

	@Test
	void saveOutside(){
		Customer customer = new Customer("first", "last");

	}
}
