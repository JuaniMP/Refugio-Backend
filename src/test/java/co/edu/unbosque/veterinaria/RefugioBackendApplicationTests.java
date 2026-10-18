package co.edu.unbosque.veterinaria;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Arranca la aplicación completa: necesita el MySQL local de database/setup.sh. */
@SpringBootTest
@ActiveProfiles("dev")
class RefugioBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
