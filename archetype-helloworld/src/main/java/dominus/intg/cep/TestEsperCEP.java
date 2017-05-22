package dominus.intg.cep;


import com.espertech.esper.client.*;
import org.junit.Test;
import origin.common.junit.DominusJUnit4TestBase;

import static org.junit.Assert.*;

public class TestEsperCEP extends DominusJUnit4TestBase {

    EPServiceProvider epService;


    @Override
    protected void doSetUp() throws Exception {
        super.doSetUp();
        epService = EPServiceProviderManager.getDefaultProvider();
    }

    @Override
    protected void doTearDown() throws Exception {
        super.doTearDown();
        epService.destroy();
    }

    @Test
    public void testSendingEvent() {
        //Creating a Statement, A statement is a continuous query registered with an Esper engine instance that provides results to listeners as new data arrives,
        // in real-time, or by demand via the iterator (pull) API.
        String expression = "select avg(price) from dominus.intg.cep.OrderEvent.win:time(30 sec)";
        EPStatement statement = epService.getEPAdministrator().createEPL(expression);
        // attaching the listener to the statement
        OrderEventListener listener = new OrderEventListener();
        statement.addListener(listener);

        OrderEvent event1 = new OrderEvent("shirt", 74.50);
        OrderEvent event2 = new OrderEvent("boot", 24.00);
        epService.getEPRuntime().sendEvent(event1);
        epService.getEPRuntime().sendEvent(event2);
        assertTrue(OrderEventListener.eventCount == 2);
    }

    //Listeners are invoked by the engine in response to one or more events that change a statement's result set.
    public static class OrderEventListener implements UpdateListener {
        static Integer eventCount = 0;

        public void update(EventBean[] newEvents, EventBean[] oldEvents) {
            EventBean event = newEvents[0];
            System.out.println("avg=" + event.get("avg(price)"));
            eventCount++;
        }
    }
}
