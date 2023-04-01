package com.example.demo;

import com.example.demo.TestsUtils;
import com.example.demo.controllers.CartController;
import com.example.demo.controllers.ItemController;
import com.example.demo.controllers.OrderController;
import com.example.demo.controllers.UserController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SareetaApplicationTests {

	UserController userController;
	CartController cartController;
	ItemController itemController;

	OrderController orderController;

	UserRepository userRepository = mock(UserRepository.class);
	CartRepository cartRepository = mock(CartRepository.class);
	 ItemRepository itemRepository  = mock(ItemRepository.class);
	OrderRepository orderRepository = mock(OrderRepository.class);
	BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);

	@Test
	public void contextLoads() {
	}

@Before
public void setUp(){
	userController = new UserController();
	cartController = new CartController();
	itemController = new ItemController();
	orderController = new OrderController();

	TestsUtils.injectObjects(userController, "userRepository", userRepository);
	TestsUtils.injectObjects(userController, "cartRepository", cartRepository);
	TestsUtils.injectObjects(userController, "bCryptPasswordEncoder", bCryptPasswordEncoder);

	TestsUtils.injectObjects(cartController, "userRepository", userRepository);
	TestsUtils.injectObjects(cartController, "cartRepository", cartRepository);
	TestsUtils.injectObjects(cartController, "itemRepository", itemRepository);

	TestsUtils.injectObjects(itemController, "itemRepository", itemRepository);

	TestsUtils.injectObjects(orderController, "orderRepository", orderRepository);
	TestsUtils.injectObjects(orderController, "userRepository", userRepository);
}

////////////////////////////////////////////////////////////////////////

	//Testing UserController Section
	@Test
	public void findById() throws Exception {
		ResponseEntity<User> responseNull = userController.findById(1L);
		Assert.assertEquals(404 , responseNull.getStatusCodeValue());
		User user = createUserForTest();

		ResponseEntity<User> response = userController.findById(user.getId());
		System.out.println(response.getBody());
		Assert.assertEquals(0 , response.getBody().getId());
		}

	@Test
	public void findByUsername() throws Exception{
		// Test not found username
		ResponseEntity<User> responseNotFound = userController.findByUserName("Shuroog");
		Assert.assertEquals(404 , responseNotFound.getStatusCodeValue());

		// Test found username
		User user = createUserForTest();
		ResponseEntity<User> responseCreation =  userController.findByUserName("Shuroog");
		Assert.assertEquals(200,responseCreation.getStatusCodeValue());

	}

	@Test
	public void createUser() throws Exception{
		CreateUserRequest user = new CreateUserRequest();
		user.setUsername("Shuroog");
		user.setPassword("123456");

		// set wrong confirm password
		user.setConfirmPassword("12345");
		ResponseEntity<User> responseFailed = userController.createUser(user);
		Assert.assertEquals(400 , responseFailed.getStatusCodeValue());

		//set the correct confirm password
		user.setConfirmPassword("123456");
		when(bCryptPasswordEncoder.encode("123456")).thenReturn("123456");
		ResponseEntity<User> responseSucss= userController.createUser(user);
		Assert.assertEquals(200 , responseSucss.getStatusCodeValue());
		User userCreated = responseSucss.getBody();
		Assert.assertEquals("Shuroog", userCreated.getUsername());
		System.out.println(userCreated.getPassword());
		Assert.assertEquals("123456", userCreated.getPassword());

	}

	public User createUserForTest() throws Exception{
		CreateUserRequest userRe = new CreateUserRequest();
		userRe.setUsername("Shuroog");
		userRe.setPassword("123456");
		userRe.setConfirmPassword("123456");

		User user = userController.createUser(userRe).getBody();
		when(userRepository.findById(0L)).thenReturn(Optional.of(user));
		when(userRepository.findByUsername("Shuroog")).thenReturn(user);
		return user;
	}

	////////////////////////////////////////////////////////////////////////

	//Testing CartController Section

	public void createCart(){
		User user = new User();
		Cart cart = new Cart();
		user.setId(1);
		user.setUsername("Shuroog");
		user.setPassword("1234567");
		user.setCart(cart);
		when(userRepository.findByUsername("Shuroog")).thenReturn(user);

		Item item = new Item();
		item.setId(1L);
		item.setName("Round Widget");
		item.setPrice(BigDecimal.valueOf(2.99));
		item.setDescription("A widget that is round");

		cart = new Cart();
		cart.setId(1L);
		cart.setUser(user);
		cart.setItems(new ArrayList<Item>());
		cart.setTotal(BigDecimal.valueOf(0.00));

		user.setCart(cart);
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
	}
	@Test
	public void addTocart () throws Exception {
		ModifyCartRequest requestUser = new ModifyCartRequest();
		// Check the result when user is null
		ResponseEntity<Cart> nullUsername = cartController.addTocart(requestUser);
		Assert.assertEquals(404  , nullUsername.getStatusCodeValue());

		createCart();
		requestUser.setUsername("Shuroog");
		//check for Item when its null
		ResponseEntity<Cart> nullItem = cartController.addTocart(requestUser);
		Assert.assertEquals(404  , nullItem.getStatusCodeValue());

		requestUser.setItemId(1);
		requestUser.setQuantity(1);

		ResponseEntity<Cart> succssfullResponce = cartController.addTocart(requestUser);
		Assert.assertEquals(200 , succssfullResponce.getStatusCodeValue());
		Cart addedCart = succssfullResponce.getBody();
		Assert.assertEquals("Shuroog", addedCart.getUser().getUsername());
		Assert.assertEquals(1, addedCart.getItems().size());

	}


	@Test
	public void removeFromcart () throws Exception {

		ModifyCartRequest requestUser = new ModifyCartRequest();
		// Check the result when user is null
		ResponseEntity<Cart> nullUsername = cartController.removeFromcart(requestUser);
		Assert.assertEquals(404  , nullUsername.getStatusCodeValue());
		createCart();
		requestUser.setUsername("Shuroog");
		//check for Item when its null
		ResponseEntity<Cart> nullItem = cartController.removeFromcart(requestUser);
		Assert.assertEquals(404  , nullItem.getStatusCodeValue());

		requestUser.setItemId(1);
		requestUser.setQuantity(1);

		ResponseEntity<Cart> succssfullResponce = cartController.removeFromcart(requestUser);
		Assert.assertEquals(200  , succssfullResponce.getStatusCodeValue());
		Cart removedCart = succssfullResponce.getBody();
		Assert.assertNotEquals(1, removedCart.getItems());
		Assert.assertNotEquals(2.99, removedCart.getTotal());
	}



	//////////////////////////////////////////
	// itemController Testing

	public void getItem(){
		Item item = new Item();
		item.setId(1L);
		item.setName("Round Widget");
		item.setPrice(BigDecimal.valueOf(2.99));
		item.setDescription("A widget that is round");
		when(itemRepository.findAll()).thenReturn(Arrays.asList(item));
		when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
		when(itemRepository.findByName("Round Widget")).thenReturn(Arrays.asList(item));

	}

	@Test
	public void getItems() {
		ResponseEntity<List<Item>> succssfullResponce = itemController.getItems();
		Assert.assertEquals(200  , succssfullResponce.getStatusCodeValue());
	}

	@Test
	public void getItemById() {
		getItem();
		ResponseEntity<Item> getIdResponce =  itemController.getItemById(1L);
		Assert.assertEquals(200  , getIdResponce.getStatusCodeValue());
		Assert.assertEquals("Round Widget", getIdResponce.getBody().getName());

	}

	@Test
	public void getItemsByName() {
		getItem();
		ResponseEntity<List<Item>> notFoundResponce = itemController.getItemsByName("");
		Assert.assertEquals(404 , notFoundResponce.getStatusCodeValue());

		ResponseEntity<List<Item>> foundResponce = itemController.getItemsByName("Round Widget");
		Assert.assertEquals(200  , foundResponce.getStatusCodeValue());
		Assert.assertEquals("Round Widget", foundResponce.getBody().get(0).getName());
	}


	////////////////////////////////////////////
	// OrderController

	public void createOrder(){
		Item item = new Item();
		item.setId(1L);
		item.setName("Round Widget");
		item.setDescription("A widget that is round");
		item.setPrice(BigDecimal.valueOf(2.99));
		List<Item> items = new ArrayList<Item>();
		items.add(item);

		User user = new User();
		user.setId(1);
		user.setUsername("Shuroog");
		user.setPassword("1234567");

		Cart cart = new Cart();
		cart.setId(1L);
		cart.setUser(user);
		cart.setItems(items);
		cart.setTotal(BigDecimal.valueOf(2.99));
		user.setCart(cart);
		when(userRepository.findByUsername("Shuroog")).thenReturn(user);
	}

	@Test
	public void submit() throws Exception {
		createOrder();
		ResponseEntity<UserOrder> nullUser = orderController.submit("");
		Assert.assertEquals(404 , nullUser.getStatusCodeValue());

		ResponseEntity<UserOrder> submitOrder = orderController.submit("Shuroog");
		Assert.assertEquals(200 , submitOrder.getStatusCodeValue());
		UserOrder order = submitOrder.getBody();
		Assert.assertEquals("Shuroog", order.getUser().getUsername());

	}
	@Test
	public void getOrdersForUser() throws Exception {
		createOrder();
		ResponseEntity<List<UserOrder>> nullUser= orderController.getOrdersForUser("");
		Assert.assertEquals(404 , nullUser.getStatusCodeValue());

		ResponseEntity<List<UserOrder>> userOrder  = orderController.getOrdersForUser("Shuroog");
		Assert.assertEquals(200 , userOrder.getStatusCodeValue());
	}
}
