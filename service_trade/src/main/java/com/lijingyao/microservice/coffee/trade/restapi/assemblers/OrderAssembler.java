package com.lijingyao.microservice.coffee.trade.restapi.assemblers;

import com.lijingyao.microservice.coffee.template.trade.OrderCreateDTO;
import com.lijingyao.microservice.coffee.template.trade.OrderDetailCreateDTO;
import com.lijingyao.microservice.coffee.trade.persistence.entity.TradeOrder;
import com.lijingyao.microservice.coffee.trade.persistence.entity.TradeOrderDetail;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Created by lijingyao on 2018/7/8 23:35.
 */
@Component
public class OrderAssembler {

    private static SecureRandom secureRandom = new SecureRandom();
    public static ZoneId CHINA = ZoneId.of("Asia/Shanghai");
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmssSS");

    private BeanCopier orderCreateDetailCopier = BeanCopier.create(OrderDetailCreateDTO.class, TradeOrderDetail.class, false);

    public TradeOrder assembleOrder(OrderCreateDTO createDTO) {

        TradeOrder order = new TradeOrder();
        order.setUserId(createDTO.getUserId());
        order.setOrderId(buildOrderId(createDTO.getUserId()));

        return order;
    }


    public String buildOrderId(Long userId) {
        return createOrderId(userId, "01");
    }

    public String buildOrderDetailId(Long userId) {
        return createOrderId(userId, "02");
    }

    private String createOrderId(Long userId, String placeholder) {
        userId = userId == null ? 0 : userId;
        String user = StringUtils.reverse(String.format("%02d", Long.valueOf(userId.longValue() % 8194)));
        String random = String.valueOf(Integer.toHexString(secureRandom.nextInt(1000000) + 1234567));
        String time = formatter.format(ZonedDateTime.now(CHINA));
        String timeStr = Long.toHexString(Long.parseLong(time));
        StringBuilder sb = new StringBuilder();
        sb.append(user).append(timeStr).append(random).append(placeholder);
        return sb.toString();
    }


    public Optional<List<TradeOrderDetail>> assembleDetailOrders(OrderCreateDTO createDTO, TradeOrder order) {
        if (createDTO != null && order != null) {
            List<OrderDetailCreateDTO> detailCreateDTOS = createDTO.getDetails();
            if (CollectionUtils.isEmpty(detailCreateDTOS)) {
                return Optional.empty();
            }
            return Optional.of(detailCreateDTOS.stream().map(detailDTO -> assembleDetailOrder(detailDTO, order)).collect(Collectors.toList()));
        }
        return Optional.empty();
    }

    private TradeOrderDetail assembleDetailOrder(OrderDetailCreateDTO detailDTO, TradeOrder order) {
        TradeOrderDetail detail = new TradeOrderDetail();

        orderCreateDetailCopier.copy(detailDTO, order, null);
        detail.setOrderId(buildOrderDetailId(order.getUserId()));
        detail.setMainOrderId(order.getOrderId());

        detail.setUserId(order.getUserId());
        detail.setAdditional(orderAddtionalBuilder(detailDTO));

        return detail;

    }

    private String orderAddtionalBuilder(OrderDetailCreateDTO detailCreateDTO) {
        StringJoiner stringJoiner = new StringJoiner("|");
        if (detailCreateDTO.getEspresso() > 0) {
            stringJoiner.add(detailCreateDTO.getEspresso() + "");
        } else {
            stringJoiner.add("-");
        }
        if (StringUtils.isNoneBlank(detailCreateDTO.getCoffeine())) {
            stringJoiner.add(detailCreateDTO.getEspresso() + "");
        } else {
            stringJoiner.add("-");
        }

        if (StringUtils.isNoneBlank(detailCreateDTO.getSugar())) {
            stringJoiner.add(detailCreateDTO.getSugar() + "");
        } else {
            stringJoiner.add("-");
        }
        if (StringUtils.isNoneBlank(detailCreateDTO.getMilk())) {
            stringJoiner.add(detailCreateDTO.getMilk() + "");
        } else {
            stringJoiner.add("-");
        }

        return stringJoiner.toString();
    }
}
