package se.citerus.cqrs.bookstore.domain.order;

import org.junit.Test;
import se.citerus.cqrs.bookstore.book.BookId;
import se.citerus.cqrs.bookstore.event.DomainEvent;
import se.citerus.cqrs.bookstore.order.CustomerInformation;
import se.citerus.cqrs.bookstore.order.OrderId;
import se.citerus.cqrs.bookstore.order.OrderLine;
import se.citerus.cqrs.bookstore.order.event.OrderActivatedEvent;
import se.citerus.cqrs.bookstore.order.event.OrderPlacedEvent;

import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.emptyIterable;
import static org.junit.Assert.assertThat;

public class OrderTest {

  private static final CustomerInformation JOHN_DOE = new CustomerInformation("John Doe", "john@acme.com", "Highway street 1");

  @Test
  public void placingAnOrder() {
    Order order = new Order();
    OrderLine orderLine = new OrderLine(BookId.<BookId>randomId(), "title", 10, 200L, null);
    order.place(OrderId.<OrderId>randomId(), JOHN_DOE, asList(orderLine));
    List<DomainEvent> uncommittedEvents = order.getUncommittedEvents();
    assertThat(uncommittedEvents.size(), is(1));
    assertThat(getOnlyElement(uncommittedEvents), instanceOf(OrderPlacedEvent.class));
  }

  @Test
  public void activatingAnOrder() {
    OrderLine orderLine = new OrderLine(BookId.<BookId>randomId(), "title", 10, 200L, null);
    Order order = new Order();
    order.place(OrderId.<OrderId>randomId(), JOHN_DOE, asList(orderLine));
    order.markChangesAsCommitted();
    order.activate();

    List<DomainEvent> uncommittedEvents = order.getUncommittedEvents();
    assertThat(uncommittedEvents.size(), is(1));
    assertThat(getOnlyElement(uncommittedEvents), instanceOf(OrderActivatedEvent.class));
  }

  @Test(expected = IllegalStateException.class)
  public void cannotPlaceAnOrderTwice() {
    Order order = new Order();
    order.place(OrderId.<OrderId>randomId(), JOHN_DOE, Collections.<OrderLine>emptyList());
    order.place(OrderId.<OrderId>randomId(), JOHN_DOE, Collections.<OrderLine>emptyList());
  }

  @Test
  public void activatingTwiceDoesNotGenerateASecondActivationEvent() {
    Order order = new Order();
    BookId bookId = BookId.randomId();
    long unitPrice = 200L;
    OrderLine orderLine = new OrderLine(bookId, "title", 10, unitPrice, null);
    order.place(OrderId.<OrderId>randomId(), JOHN_DOE, asList(orderLine));

    order.markChangesAsCommitted();
    order.activate();
    order.markChangesAsCommitted();
    order.activate();
    assertThat(order.getUncommittedEvents(), emptyIterable());
  }

}
