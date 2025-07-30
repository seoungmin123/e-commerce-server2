package kr.hhplus.be.server.product.service;


import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.order.domain.IOrderRepository;
import kr.hhplus.be.server.order.dto.OrderCreateCommand;
import kr.hhplus.be.server.product.domain.IProductRepository;
import kr.hhplus.be.server.product.domain.Product;
import kr.hhplus.be.server.product.domain.ProductStock;
import kr.hhplus.be.server.product.dto.PopularProductInfo;
import kr.hhplus.be.server.product.dto.PopularProductQuery;
import kr.hhplus.be.server.product.dto.ProductInfo;
import kr.hhplus.be.server.product.dto.ValidatedProductInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static kr.hhplus.be.server.common.exception.ApiErrorCode.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final IProductRepository productRepository;
    private final IOrderRepository orderRepository;

    // 상품 전체조회
    @Transactional(readOnly = true)
    public List<ProductInfo> getAllProducts() {
        List<Product> products = productRepository.findAll();

        if (products == null || products.isEmpty()) {
            return Collections.emptyList(); // null 또는 빈 리스트일 때 빈 리스트 반환
        }

        return products.stream().map(ProductInfo::from).toList();
    }

    // 인기상품 조회 : 상위 5개
    @Transactional(readOnly = true)
    public List<PopularProductInfo> getTopFivePopularProducts() {
        List<PopularProductQuery> productQueries = orderRepository.findTopFivePopularProducts();
        AtomicLong rank = new AtomicLong(1);
        return productQueries.stream()
                .map(query -> query.toInfo(rank.getAndIncrement()))
                .collect(Collectors.toList());
    }

    // 상품 유효성 검증
    @Transactional(readOnly = true)
    public List<ValidatedProductInfo> validateProducts(List<OrderCreateCommand.OrderItemCommand> orderItemCommands) {

        // 상품 아이템 아이디 목록
        List<Long> productIds = new ArrayList<>();
        for (OrderCreateCommand.OrderItemCommand cmd : orderItemCommands) {
            productIds.add(cmd.productId());
        }
        // 상품 아이템 존재하는지 확인
        List<Product> products = productRepository.findAllById(productIds);

        // 입력받은 상품 아이디 , 검증 사이즈 비교
        if (products.size() != productIds.size()) {
            throw new ApiException(NOT_FOUND);
        }

        // 상품정보 반환
        List<ValidatedProductInfo> validatedProducts = new ArrayList<>();

        for (Product product : products) {
            // 1. 현재 product에 해당하는 주문 커맨드 찾기
            OrderCreateCommand.OrderItemCommand matchingCommand = null;

            for (OrderCreateCommand.OrderItemCommand command : orderItemCommands) {
                if (command.productId().equals(product.getId())) {
                    matchingCommand = command;
                    break;
                }
            }

            // 2. 못 찾았으면 예외
            if (matchingCommand == null) {
                throw new ApiException(NOT_FOUND);
            }

            // 3. 찾았으면 ValidatedProductInfo 생성해서 리스트에 추가
            ValidatedProductInfo validated = ValidatedProductInfo.of(product, matchingCommand.quantity());
            validatedProducts.add(validated);
        }

        return validatedProducts;
    }

    // 상품 재고차감
    public void deductStock(List<OrderCreateCommand.OrderItemCommand> commands) {
        List<Long> productIds = commands.stream()
                .map(OrderCreateCommand.OrderItemCommand::productId)
                .collect(Collectors.toList());

        // fixme : in with lock
        List<ProductStock> stocks = productRepository.findAllByIdsWithLock(productIds);

        Map<Long, ProductStock> stockMap = stocks.stream()
                .collect(Collectors.toMap(ProductStock::getId, stock -> stock));

        for (OrderCreateCommand.OrderItemCommand command : commands) {
            ProductStock stock = stockMap.get(command.productId());
            stock.deduct(command.quantity());
        }

        productRepository.saveAll(stocks);
    }

}