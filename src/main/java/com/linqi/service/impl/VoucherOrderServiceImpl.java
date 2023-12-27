package com.linqi.service.impl;

import com.linqi.dto.Result;
import com.linqi.entity.SeckillVoucher;
import com.linqi.entity.VoucherOrder;
import com.linqi.mapper.VoucherOrderMapper;
import com.linqi.service.ISeckillVoucherService;
import com.linqi.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linqi.utils.RedisIdWorker;
import com.linqi.utils.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author LINQI
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Override
    @Transactional
    public Result seckillVoucher(Long voucherId) {
        // 1。查询优惠券
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        // 2.判断秒杀是否开始
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀未开始");
        }
        // 3.判断秒杀是否结束
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已经结束，请下次再来!");
        }
        // 4.判断库存是否充足
        if (voucher.getStock() < 1) {
            return Result.fail("库存不足！");
        }

        return createVoucherOrder(voucherId);
    }

    @Transactional
    public synchronized Result createVoucherOrder(Long voucherId) {
        // 5.一人一单
        Long userId = UserHolder.getUser().getId();
        Long count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();

        if (count > 0) {
            return Result.fail("用户已经购买过一次了！");
        }

        // 6.扣减库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId).
                gt("stock", 0).
                update();
        // 7.判断库存是否扣减成功,扣减失败，返回库存为空的信息
        if (!success) {
            return Result.fail("库存不足！");
        }

        // 8.扣减成功了，创建订单
        VoucherOrder voucherOrder = new VoucherOrder();

        // 8.1 生成订单号
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);

        // 8.2 生成用户 id
        voucherOrder.setUserId(userId);

        // 8.3 代金券 id
        voucherOrder.setVoucherId(voucherId);

        // 8.4 订单写入数据库
        save(voucherOrder);

        // 9.返回订单信息
        return Result.ok(orderId);
    }
}
