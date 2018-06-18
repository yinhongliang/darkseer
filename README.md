# darkseer
Hive Data ORM
基于RPC + ORM模式实现的Hive数据访问组件，支持多客户端，多服务端，底层数据通信基于Redis Read/Write和Pub/Sub。<br>
<br>
工作流程<br>
1.注册服务<br>
1.a)Server Agent订阅上行消息;<br>
<br>
2.发起查询<br>
2.a)解析业务层参数和Sql模板;<br>
2.b)拼接Hive Sql;<br>
2.c)写入Hive Sql到redis;<br>
2.d)Client Agent订阅下行消息;<br>
2.e)发送发起查询消息;<br>
<br>
3.执行查询<br>
3.a)Server Agent过滤非关注业务消息;<br>
3.b)读取Hive Sql;<br>
3.c)调用本地执行脚本;<br>
3.d)写入执行结果到Redis;<br>
3.e)发送查询响应消息;<br>
<br>
4.返回查询结果<br>
4.a)Client Agent过滤非关注业务消息;<br>
4.b)读取查询数据表头，和查询数据;<br>
4.c)封装为Java对象或Java对象列表;
