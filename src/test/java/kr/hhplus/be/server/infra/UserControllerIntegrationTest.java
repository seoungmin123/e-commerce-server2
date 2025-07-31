package kr.hhplus.be.server.infra;


import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.point.controller.PointChargeRequest;
import kr.hhplus.be.server.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {"file:./init/01-cleanup.sql"
        , "file:./init/05-product_popularity_dummy.sql"})
class UserControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 포인트_충전시_200_응답이_반환된다() throws Exception {
        // given
        User user = User.create("테스트유저");

        PointChargeRequest request = new PointChargeRequest(user, BigDecimal.valueOf(10000));

        // when & then
        mockMvc.perform(put("/users/1/point")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").exists())
                .andDo(print());
    }

    @Test
    void 포인트_조회시_200_응답이_반환된다() throws Exception {
        mockMvc.perform(get("/users/1/point"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").exists())
                .andDo(print());
    }

    @Test
    void 쿠폰_목록_조회시_200_응답이_반환된다() throws Exception {
        mockMvc.perform(get("/users/1/coupons"))
                .andExpect(status().isOk())
                .andDo(print());
    }
}