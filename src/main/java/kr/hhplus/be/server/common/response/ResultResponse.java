package kr.hhplus.be.server.common.response;

public record ResultResponse(
        Boolean result,
        String message
) {
    public ResultResponse {
        result = (result != null) ? result : false;
    }

    public static ResultResponse success() {
        return new ResultResponse(true, null);
    }

    public static ResultResponse fail(String message) {
        return new ResultResponse(false, message);
    }
}