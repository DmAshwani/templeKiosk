package in.dataman.transactionService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import in.dataman.Enums.PaymentStatus;
import in.dataman.transactionEntity.PaymentDetail;
import in.dataman.transactionRepo.DonationRepository;
import in.dataman.transactionRepo.PaymentDetailRepository;

@Service
public class RazorpayService {

	private final RazorpayClient razorpayClient;
	private final PaymentDetailRepository paymentDetailRepository;
	private final DonationRepository donationRepository;

	private static final String RAZORPAY_KEY_SECRET = "el24erdmUWFxb1Mz7U3vGgV1";

	public RazorpayService(RazorpayClient razorpayClient, PaymentDetailRepository paymentDetailRepository,
			DonationRepository donationRepository) {
		this.razorpayClient = razorpayClient;
		this.paymentDetailRepository = paymentDetailRepository;
		this.donationRepository = donationRepository;
	}

	// Create Razorpay Order
	public String createOrder(Double amount, String currency, String receipt) throws RazorpayException {
		JSONObject orderRequest = new JSONObject();
		orderRequest.put("amount", amount * 100); // Amount in paise
		orderRequest.put("currency", currency);
		orderRequest.put("receipt", receipt);
		orderRequest.put("payment_capture", 1);

		Order order = razorpayClient.orders.create(orderRequest);
		return order.get("id").toString();
	}


	public Map<String, String> verifyPayment(String orderId) {
		try {
			// ✅ Fetch order details from Razorpay
			Order order = razorpayClient.orders.fetch(orderId);
			System.out.println("Order Details: " + order);

			// ✅ Fetch payment details using orderId
			List<Payment> payments = razorpayClient.orders.fetchPayments(orderId);
			System.out.println("Payments: " + payments);

			if (payments.isEmpty()) {
				return Map.of("error", "No payment found for this order");
			}

			// ✅ Fetch the first (latest) payment
			Payment payment = payments.get(0);
			String paymentId = payment.get("id");
			String status = payment.get("status");
			String signature = payment.has("signature") ? payment.get("signature") : null;
			String paymentMethod = payment.get("method");

			// ✅ Validate payment signature (if available)
			if (signature != null) {
				JSONObject options = new JSONObject();
				options.put("razorpay_payment_id", paymentId);
				options.put("razorpay_order_id", orderId);
				options.put("razorpay_signature", signature);

				boolean isValid = Utils.verifyPaymentSignature(options, RAZORPAY_KEY_SECRET);
				if (!isValid) {
					return Map.of("error", "Invalid Signature Verification");
				}
			}

			
			  // ✅ Fetch PaymentDetail using orderId
            Optional<PaymentDetail> paymentDetailOpt = paymentDetailRepository.findByResTransRefId(orderId);
            if (paymentDetailOpt.isEmpty()) {
                return Map.of("error", "No payment details found for order ID: " + orderId);
            }

            PaymentDetail paymentDetail = paymentDetailOpt.get();

            // ✅ Update payment status
            switch (status) {
                case "captured":
                    paymentDetail.setStatus(PaymentStatus.Success.getCode());
                    break;
                case "failed":
                    paymentDetail.setStatus(PaymentStatus.Fail.getCode());
                    break;
                default:
                    paymentDetail.setStatus(PaymentStatus.Pending.getCode());
                    break;
            }

            paymentDetail.setResPayMode(paymentMethod);
            paymentDetail.setResBankTransrefNo(paymentId);
            paymentDetailRepository.save(paymentDetail);

            // ✅ Update donation status if exists
            donationRepository.findById(paymentDetail.getDocId()).ifPresent(donation -> {
                donation.setStatus(paymentDetail.getStatus());
                donationRepository.save(donation);
            });

			

			return Map.of("paymentId", paymentId, "status", status, "message", "Payment status updated successfully");
		} catch (Exception e) {
			return Map.of("error", "Payment Verification Failed: " + e.getMessage());
		}
	}

	
	

}
