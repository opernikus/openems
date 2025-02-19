import { Edge } from "../../edge/edge";
import { JsonrpcRequest } from "../base";

/**
 * <pre>
 * {
 *   "jsonrpc": "2.0",
 *   "id": UUID,
 *   "method": "subscribeEdges",
 *   "params": {
 *      "edges": string[]
 *   }
 * }
 * </pre>
 */
export class SubscribeEdgesRequest extends JsonrpcRequest {

    static METHOD: string = "subscribeEdges";

    public constructor(
        public readonly params: {
            edges: string[]
        }
    ) {
        super(SubscribeEdgesRequest.METHOD, params);
    }

}