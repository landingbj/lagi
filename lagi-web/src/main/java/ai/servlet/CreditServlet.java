package ai.servlet;

import ai.dto.ChargeDetail;
import ai.dto.CreditUserBalance;
import ai.dto.PrepayRequest;
import ai.dto.PrepayResponse;
import ai.migrate.service.BoxLandingAccountResolver.BoxBillingException;
import ai.migrate.service.CreditService;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CreditServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    protected Gson gson = new Gson();
    private final CreditService creditService = new CreditService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        try {
            if (method.equals("getSharedChargeDetails")) {
                this.getSharedChargeDetails(resp);
            } else if (method.equals("getSharedBalance")) {
                this.getSharedBalance(resp);
            } else if (method.equals("getChargeDetailBySeq")) {
                this.getChargeDetailBySeq(req, resp);
            } else {
                writeFailure(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "credit endpoint not found");
            }
        } catch (BoxBillingException e) {
            log.warn("Box billing account is unavailable: code={}", e.getCode(), e);
            writeFailure(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            writeFailure(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_REQUEST", e.getMessage());
        } catch (IOException e) {
            log.error("SaaS credit request failed: method={}", method, e);
            writeFailure(resp, HttpServletResponse.SC_BAD_GATEWAY, "SAAS_PAYMENT_UNAVAILABLE",
                    "the SaaS payment service is temporarily unavailable");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);

        try {
            if (method.equals("prepay")) {
                this.prepay(req, resp);
            } else {
                writeFailure(resp, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "credit endpoint not found");
            }
        } catch (BoxBillingException e) {
            log.warn("Box billing account is unavailable: code={}", e.getCode(), e);
            writeFailure(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            writeFailure(resp, HttpServletResponse.SC_BAD_REQUEST, "INVALID_REQUEST", e.getMessage());
        } catch (IOException e) {
            log.error("SaaS credit request failed: method={}", method, e);
            writeFailure(resp, HttpServletResponse.SC_BAD_GATEWAY, "SAAS_PAYMENT_UNAVAILABLE",
                    "the SaaS payment service is temporarily unavailable");
        }
    }

    private void prepay(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrepayRequest prepayRequest = reqBodyToObj(req, PrepayRequest.class);
        PrepayResponse prepayResponse = creditService.prepay(prepayRequest);
        responsePrint(resp, gson.toJson(prepayResponse));
    }

    private void getSharedChargeDetails(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        List<ChargeDetail> chargeDetails = creditService.getSharedChargeDetails();
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("data", publicChargeDetails(chargeDetails));
        responsePrint(resp, gson.toJson(result));
    }

    private void getSharedBalance(HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        CreditUserBalance creditUserBalance = creditService.getSharedBalance();
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        Map<String, Object> data = new HashMap<>();
        data.put("balance", creditUserBalance == null ? null : creditUserBalance.getBalance());
        result.put("data", data);
        responsePrint(resp, gson.toJson(result));
    }

    private void getChargeDetailBySeq(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String seq = req.getParameter("seq");
        ChargeDetail chargeDetail = creditService.getSharedChargeDetailBySeq(seq);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("data", publicChargeDetail(chargeDetail));
        responsePrint(resp, gson.toJson(result));
    }

    private List<Map<String, Object>> publicChargeDetails(List<ChargeDetail> chargeDetails) {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        if (chargeDetails != null) {
            for (ChargeDetail chargeDetail : chargeDetails) {
                result.add(publicChargeDetail(chargeDetail));
            }
        }
        return result;
    }

    private Map<String, Object> publicChargeDetail(ChargeDetail chargeDetail) {
        Map<String, Object> result = new HashMap<>();
        if (chargeDetail != null) {
            result.put("seq", chargeDetail.getSeq());
            result.put("amount", chargeDetail.getAmount());
            result.put("time", chargeDetail.getTime());
            result.put("status", chargeDetail.getStatus());
        }
        return result;
    }

    private void writeFailure(HttpServletResponse resp, int status, String code, String message) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        resp.setStatus(status);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "failed");
        result.put("code", code);
        result.put("msg", message);
        responsePrint(resp, gson.toJson(result));
    }
}
