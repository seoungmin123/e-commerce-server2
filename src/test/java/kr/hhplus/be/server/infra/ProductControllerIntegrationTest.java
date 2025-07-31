package kr.hhplus.be.server.infra;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {"file:./init/01-cleanup.sql"
        , "file:./init/05-product_popularity_dummy.sql"})
class ProductControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void 상품_목록_조회시_200_응답이_반환된다() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andDo(print());
    }


    @Test
    void 인기상품_조회시_200_응답이_반환된다() throws Exception {
        mockMvc.perform(get("/products/popular/top5"))
                .andExpect(status().isOk())
                .andDo(print());
    }
}
