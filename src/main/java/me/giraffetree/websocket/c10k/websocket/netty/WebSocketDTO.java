package me.giraffetree.websocket.c10k.websocket.netty;

import lombok.Data;

/**
 * {"seq":"0","cmd":"pong","response":{"code":200}}
 *
 * @author GiraffeTree
 * @date 2021/3/25 13:15
 */
@Data
public class WebSocketDTO {

    private String seq;
    private String cmd;
    private Response response;

    @Data
    public static class Response {
        private int code;

        public Response(int code) {
            this.code = code;
        }
    }

    public WebSocketDTO(String cmd) {
        this.cmd = cmd;
        this.seq = "0";
        this.response = new Response(200);
    }

    public WebSocketDTO(String seq, String cmd) {
        this.seq = seq;
        this.cmd = cmd;
    }

    public WebSocketDTO(String seq, String cmd, Response response) {
        this.seq = seq;
        this.cmd = cmd;
        this.response = response;
    }
}
