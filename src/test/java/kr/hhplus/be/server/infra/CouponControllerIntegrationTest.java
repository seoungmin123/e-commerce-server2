package kr.hhplus.be.server.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {"file:./init/01-cleanup.sql"
        , "file:./init/05-product_popularity_dummy.sql"})
class CouponControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_ID = "USER-ID";
    private static final String TEST_USER_ID = "1";

    @Test
    void 인증된_사용자의_쿠폰_발급시_200_응답이_반환된다() throws Exception {
        // given
        Long couponId = 1L;

        // when & then
        mockMvc.perform(post("/coupons/{couponId}/issue", couponId)
                        .header(USER_ID, TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void 내_쿠폰_목록_조회시_200_응답이_반환된다() throws Exception {
        // when & then
        mockMvc.perform(get("/coupons/my")
                        .header(USER_ID, TEST_USER_ID))
                .andExpect(status().isOk())
                .andDo(print());
    }
}