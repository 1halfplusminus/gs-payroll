package nc.asura.restapi;


import java.util.List;
import java.util.stream.Collectors;


import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
public class OrderController {

    private final OrderModelAssembler assembler;

    private final OrderRepository orderRepository;

    public OrderController(OrderModelAssembler assembler, OrderRepository orderRepository) {
        this.assembler = assembler;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/orders")
    public CollectionModel<EntityModel<Order>> all() {
        List<EntityModel<Order>> orders = orderRepository.findAll().stream()
                .map(assembler::toModel).collect(Collectors.toList());

        return CollectionModel.of(orders, linkTo(methodOn(OrderController.class).all()).withSelfRel());
    }

    @GetMapping("/orders/{id}")
    public EntityModel<?> one(@PathVariable Long id) {
        var entity = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return assembler.toModel(entity);
    }

    @PostMapping("/orders")
    public ResponseEntity<EntityModel<Order>> newOrder(@RequestBody Order newOrder) {
        newOrder.setStatus(Status.IN_PROGRESS);
        Order order = orderRepository.save(newOrder);
        return ResponseEntity.created(linkTo(methodOn(OrderController.class).one(order.getId())).toUri())
                .body(assembler.toModel(order));
    }

    @DeleteMapping("/orders/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id){
        Order order = orderRepository.findById(id).orElseThrow(()-> new OrderNotFoundException(id));
        if(order.getStatus() == Status.IN_PROGRESS){
            order.setStatus(Status.CANCELLED);
            return ResponseEntity.ok(assembler.toModel(order));
        } else {
            return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
            .body(Problem.create().withTitle("Method not allowed")
            .withDetail("You can't cancel an order that is in the" + order.getStatus() + " status"));
        }
       
    }
    @PutMapping("/orders/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Long id){
        return ResponseEntity.ok().build();
    }
}
