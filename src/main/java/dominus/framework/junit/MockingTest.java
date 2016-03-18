package dominus.framework.junit;

import org.junit.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class MockingTest {

    @Test
    public void testMockito() {
        List mockedList = mock(List.class);
        mockedList.clear();
        mockedList.add(3);
        verify(mockedList).clear();
    }


}
