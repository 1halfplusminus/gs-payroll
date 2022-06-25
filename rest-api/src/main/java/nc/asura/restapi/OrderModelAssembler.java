package nc.asura.restapi;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import org.springframework.hateoas.EntityModel;

@Component
public class OrderModelAssembler implements RepresentationModelAssembler<Order, EntityModel<Order>> {

    @Override
    public EntityModel<Order> toModel(Order entity) {
        EntityModel<Order> entityModel = EntityModel.of(entity,
                linkTo(methodOn(OrderController.class).one(entity.getId())).withSelfRel());

        if (entity.getStatus() == Status.IN_PROGRESS) {
            entityModel
                    .add(linkTo(methodOn(OrderController.class).cancel(entity.getId()))
                            .withRel("cancel"));

            entityModel.add(linkTo(methodOn(OrderController.class).complete(entity.getId()))
            .withRel("complete"));
        }
        return entityModel;
    }

}
