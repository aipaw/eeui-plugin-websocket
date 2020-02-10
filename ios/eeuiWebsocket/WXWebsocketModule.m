
//
//  WXWebsocketModule.m
//  AFNetworking
//

#import "WXWebsocketModule.h"
#import <WeexPluginLoader/WeexPluginLoader.h>
#import <SocketRocket/SRWebSocket.h>

@interface WXWebsocketModule ()<SRWebSocketDelegate>

@property (nonatomic, strong) SRWebSocket *socket;
@property (nonatomic, strong) WXModuleKeepAliveCallback webCallback;

@end

@implementation WXWebsocketModule

WX_PlUGIN_EXPORT_MODULE(eeuiWebsocket, WXWebsocketModule)
WX_EXPORT_METHOD(@selector(connect:callback:))
WX_EXPORT_METHOD(@selector(send:))
WX_EXPORT_METHOD(@selector(stop))
WX_EXPORT_METHOD_SYNC(@selector(state))

@synthesize weexInstance;

//连接
-(void)connect:(NSString*)url  callback:(WXModuleKeepAliveCallback)callback {
    self.webCallback = callback;
    self.socket.delegate = nil;
    [self.socket close];
    
    self.socket = [[SRWebSocket alloc] initWithURLRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:url]]];
    self.socket.delegate = self;
    [self.socket open];
}

//发送消息
-(void)send:(NSString*)msg {
    if (self.socket == nil) {
        return;
    }
    [self.socket send:msg];
}

//断开连接
-(void)stop {
    if (self.socket == nil) {
        return;
    }
    [self.socket close];
    self.socket = nil;
}

//获取状态
-(int)state {
    if (self.socket == nil) {
        return 0;
    }
    if (self.socket.readyState == SR_OPEN) {
        return 1;
    }
    return 0;
}

//长链接已连接成功
- (void)webSocketDidOpen:(SRWebSocket *)webSocket{
    if (self.webCallback == nil) {
        return;
    }
    self.webCallback(@{@"status":@"open",@"msg":@""}, YES);
}

//请求长链接失败 及其原因
- (void)webSocket:(SRWebSocket *)webSocket didFailWithError:(NSError *)error{
    self.socket = nil;
    if (self.webCallback == nil) {
        return;
    }
    self.webCallback(@{@"status":@"failure",@"msg":[error localizedDescription]}, NO);
}

//长链接收到消息
- (void)webSocket:(SRWebSocket *)webSocket didReceiveMessage:(id)message {
    NSString *msg = (NSString *)message;
    if (self.webCallback == nil) {
        return;
    }
    self.webCallback(@{@"status":@"message",@"msg":msg}, YES);
}

//长链接断开 及其原因
- (void)webSocket:(SRWebSocket *)webSocket didCloseWithCode:(NSInteger)code reason:(NSString *)reason wasClean:(BOOL)wasClean{
    self.socket.delegate = nil;
    self.socket = nil;
    if (self.webCallback == nil) {
        return;
    }
    self.webCallback(@{@"status":@"closed",@"msg":@{@"code":@(code),@"reason":reason}}, NO);
}

@end
