package kr.hhplus.be.server.product.service;


import kr.hhplus.be.server.common.exception.ApiException;
import kr.hhplus.be.server.order.domain.IOrderRepository;
import kr.hhplus.be.server.order.dto.OrderCommand;
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

        List<Long> productsIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        //상품 재고 정보
        List<ProductStock> productStocks = productRepository.findAllByProductIds(productsIds);


        // 상품 재고 정보 추출 k,v
        Map<Long, ProductStock> stockMap = productStocks.stream()
                .collect(Collectors.toMap(stock -> stock.getProduct().getId(), stock -> stock));

        return products.stream().map(product -> {
            ProductStock stock = stockMap.get(product.getId());
            return ProductInfo.of(product, stock);
        }).toList();
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
    public List<ValidatedProductInfo> validateProducts(List<OrderCommand.Item> orderItemCommands) {

        // 상품 아이템 아이디 목록 추출
        List<Long> productIds = new ArrayList<>();
        for (OrderCommand.Item cmd : orderItemCommands) {
            productIds.add(cmd.productId());
        }

        // 상품 아이템 존재하는지 확인
        List<Product> products = productRepository.findAllById(productIds);

        // 입력받은 상품 아이디 , 검증 사이즈 비교
        if (products.size() != productIds.size()) {
            throw new ApiException(NOT_FOUND);
        }

        // 주문 커맨드 productId 기준으로 상품정보 추출 k,v (성능 최적화)
        Map<Long, OrderCommand.Item> commandMap = orderItemCommands.stream()
                .collect(Collectors.toMap(OrderCommand.Item::productId, product -> product));

        // 상품 + 수량으로 ValidatedProductInfo 생성
        return products.stream()
                .map(product -> {
                    OrderCommand.Item command = commandMap.get(product.getId());
                    if (command == null) {
                        throw new ApiException(NOT_FOUND);
                    }
                    return ValidatedProductInfo.of(product, command.quantity());
                })
                .collect(Collectors.toList());
    }

    // 상품 재고차감 비관적락 적용
    @Transactional
    public void deductStock(List<OrderCommand.Item> commands) {
        // 주문하려는 상품 ID들 추출
        List<Long> productIds = commands.stream()
                .map(OrderCommand.Item::productId)
                .collect(Collectors.toList());

        // DB에서 재고 정보를 읽어옴 (PESSIMISTIC_WRITE)
        // 재고 정보 리스트
        List<ProductStock> stocks = productRepository.findAllByIdsWithLock(productIds);

        //주문 id 갯수 != 상품 조회 id 갯수
        if (stocks.size() != productIds.size()){
            throw new ApiException(NOT_FOUND);
        }

        //상품주문 ids , 재고정보
        Map<Long, ProductStock> stockMap = stocks.stream()
                .collect(Collectors.toMap(ProductStock::getId, stock -> stock));

        //주문아이템의 상품정보 재고차감
        for (OrderCommand.Item command : commands) {
            ProductStock stock = stockMap.get(command.productId());
            stock.deduct(command.quantity());
        }

        productRepository.saveAll(stocks);
    }

}