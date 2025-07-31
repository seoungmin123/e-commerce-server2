package kr.hhplus.be.server.infra;


import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.order.controller.OrderCreateRequest;
import kr.hhplus.be.server.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {"file:./init/01-cleanup.sql", "file:./init/03-test-data.sql"})
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 주문_생성_요청이_성공하면_200_응답이_반환된다() throws Exception {
        // given
        User user = User.create("테스트유저");
        ReflectionTestUtils.setField(user, "id", 1L); // 수동 설정

        OrderCreateRequest request = new OrderCreateRequest(
                user,
                List.of(new OrderCreateRequest.OrderProductRequest(1L, 1)),
                null
        );

        // when & then
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.status").exists())
                .andDo(print());
    }
}