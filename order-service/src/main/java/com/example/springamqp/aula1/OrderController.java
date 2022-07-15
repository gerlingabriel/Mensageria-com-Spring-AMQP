package com.example.springamqp.aula1;

import java.util.Collection;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/orders")
public class OrderController {

	@Autowired
	private OrderRepository orders;

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@PostMapping
	public Order create(@RequestBody Order order) {
		orders.save(order);

		// String filaRabbitmq = "orders.v1.order-created";		
		// Message message = new Message(order.getId().toString().getBytes());
		// rabbitTemplate.send(filaRabbitmq, message);

		OrderCreateEvent event = new OrderCreateEvent(order.getId(), order.getValue());
		rabbitTemplate.convertAndSend("orders.v1.order-created", "",event);

		return order;
	}

	@GetMapping
	public Collection<Order> list() {
		return orders.findAll();
	}

	@GetMapping("/{id}")
	public Order findById(@PathVariable Long id) {
		return orders.findById(id).orElseThrow();
	}

	@PutMapping("{id}/pay")
	public Order pay(@PathVariable Long id) {
		Order order = orders.findById(id).orElseThrow();
		order.markAsPaid();
		return orders.save(order);
	}
	
}
