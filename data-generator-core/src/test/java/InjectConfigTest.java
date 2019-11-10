import com.cooperl.injector.core.config.InjectorConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class InjectConfigTest {

    @InjectMocks
    private InjectorConfig injectorConfig;

    @Test
    void getBeansClassNameTest() {
        injectorConfig = new InjectorConfig();
    }
}
