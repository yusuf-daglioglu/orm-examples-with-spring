package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/demo")
public class MainController {

    @Autowired
    private EntityManager entityManager;

    // URL
    // http://localhost:8081/orm-demo/demo/simple_example_return_all_entity_columns
    @GetMapping("/simple_example_return_all_entity_columns")
	public List<ShoppingOrderEntity> simple_example_return_all_entity_columns() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        /*
        Whole SQL:

        SELECT * FROM Shopping_Order as so
        WHERE so.name LIKE '%my%'
        ORDER by so.id DESC
         */

        // The result of whole SQL is ShoppingOrderEntity class.
        // If we don't pass the "ShoppingOrderEntity.class" parameter then we have to cast the result at the end of whole code.
        // Check other examples for the cast operation.
        CriteriaQuery<ShoppingOrderEntity> criteriaQuery = builder.createQuery(ShoppingOrderEntity.class);

        // This part only:
        // FROM ShoppingOrderEntity so
        Root<ShoppingOrderEntity> so = criteriaQuery.from(ShoppingOrderEntity.class);

        // condition part
        Predicate whereName = builder.like(so.get("name"), "%my%");
        criteriaQuery.where(whereName);
        // criteriaQuery.where(whereName, whereId); // we could have multiple conditions here...

        // sorting part:
        Order order = builder.desc(so.get("id"));
        criteriaQuery.orderBy(order);

        // generate JPQL String
        TypedQuery<ShoppingOrderEntity> typedQuery = entityManager.createQuery(criteriaQuery);

        // execute JPQL.
        List<ShoppingOrderEntity> resultList =  typedQuery.getResultList();

        removeNestedRecursiveObject(resultList);
        return resultList;
	}

    /*
    URL
    http://localhost:8081/orm-demo/demo/with_specific_columns

    This is same example as above. Most of the source code is same as above.
    I change only required lines which I add comment on each of them.

    In this example we only return some columns of entity.
    On the above example we were getting all columns of entity.
     */
	@GetMapping("/with_specific_columns")
	public List<Object[]> with_specific_columns() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        /*
        Whole SQL:

        SELECT so.id, so.name FROM Shopping_Order as so   -- ===> This line updated (as diff with above example)
        WHERE so.name LIKE '%my%'
        ORDER by so.id DESC
         */

        // we remove the generic
        CriteriaQuery criteriaQuery = builder.createQuery();

        // NO CHANGE
        Root<ShoppingOrderEntity> so = criteriaQuery.from(ShoppingOrderEntity.class);

        // NO CHANGE
        Predicate whereName = builder.like(so.get("name"), "%my%");
        criteriaQuery.where(whereName);

        // NO CHANGE
        Order order = builder.desc(so.get("id"));
        criteriaQuery.orderBy(order);

        // We choose which columns we cant to return.
        // This is the part of SQL:
        // SELECT id, name
        criteriaQuery.multiselect(so.get("id"), so.get("name"));

        // We removed the generic
        TypedQuery typedQuery = entityManager.createQuery(criteriaQuery);

        // We replace the generic with the java-pojo which includes only columns of result of SQL.
        List<Object[]> resultList =  typedQuery.getResultList();

        return resultList;
	}

    /*
    URL
    http://localhost:8081/orm-demo/demo/join

    This is same example as above. Most of the source code is same as above.
    I change only required lines which I add comment on each of them.

    In this example we add join in addition to above example.
     */
	@GetMapping("/join")
	public List<Object[]> join() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();

        /*
        Whole SQL:

        SELECT so.id, so.name, i.id, i.name FROM Shopping_Order as so   -- ====> This line updated (as diff with above example)
        RIGHT JOIN Item as i                                            -- ====> This line added (as diff with above example)
        WHERE so.name LIKE '%my%'
              AND i.shopping_order_id = so.id                           -- ====> This line added (as diff with above example)
        ORDER by so.id DESC
         */

        // NO CHANGE
        CriteriaQuery criteriaQuery = builder.createQuery();

        // NO CHANGE
        Root<ShoppingOrderEntity> so = criteriaQuery.from(ShoppingOrderEntity.class);

        // Join
        Join<ItemEntity, ShoppingOrderEntity> join = so.join("itemList", JoinType.RIGHT);
        // we don't have to use "join" object anywhere. optionally, you can add other conditions to join:
        // join.on(builder.greaterThan(join.get("id"), 5)); // This "id" is the id of "item" table.

        // NO CHANGE
        Predicate whereName = builder.like(so.get("name"), "%my%");
        criteriaQuery.where(whereName);

        // NO CHANGE
        Order order = builder.desc(so.get("id"));
        criteriaQuery.orderBy(order);

        // We only add other columns from new table.
        criteriaQuery.multiselect(so.get("id"), so.get("name"), join.get("id"), join.get("name"));

        // NO CHANGE
        TypedQuery typedQuery = entityManager.createQuery(criteriaQuery);

        // NO CHANGE
        List<Object[]> resultList =  typedQuery.getResultList();

        return resultList;
	}

    private void removeNestedRecursiveObject(List<ShoppingOrderEntity> resultList) {
        resultList.forEach(shoppingOrder -> {
            if(!CollectionUtils.isEmpty(shoppingOrder.getItemList())){
                shoppingOrder.getItemList().forEach(item -> item.setShoppingOrder(null));
            }
        });
    }
}