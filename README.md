# distributed-lock
基于redis和zookeeper分布式工具集-包括:分布式接口幂等，分布式锁实现,分布式速率限制器,分布式ID生成器等.

##基于Redis实现接口幂等
~~~ 
try {
        //等待在一个信号量上，挂起
        beginCount.await();
        boolean result=RedisReentrantLock.getInstance().tryLock(commidityId1.toString(),0,TimeUnit.MILLISECONDS);
        if(!result){
            System.out.println("幂等验证：正在处理中");
            return;
        }
        System.out.println("处理业务逻辑");
    }  catch (InterruptedException e) {
        e.printStackTrace();
    }finally {
        endCount.countDown();
    }
~~~ 


##基于Redis实现的分布式锁(可重入)
~~~ java
boolean result = RedisReentrantLock.getInstance().tryLock(objectValue,cacheLock.expireTime(), TimeUnit.MILLISECONDS);
		if(!result){//取锁失败
			ERROR_COUNT += 1;
			throw new CacheLockException("get lock fail");
		}
		try{
			//执行方法
			return method.invoke(proxied, args);
		}finally{
			System.out.println("intecepor 释放锁");
			RedisReentrantLock.getInstance().unlock(objectValue);//释放锁
		}
~~~ 

##基于Zookeeper实现的分布式锁( 可重入 )
~~~ java
public static void main(String[] args){
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
    CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", retryPolicy);
    client.start();

    final ZkDistributedLockTemplate template=new ZkDistributedLockTemplate(client);//本类多线程安全,可通过spring注入
    template.execute("订单流水号", 5000, new Callback() {//获取锁超时时间为5秒
        @Override
        public Object onGetLock() throws InterruptedException {
            //TODO 获得锁后要做的事
            return null;
        }

        @Override
        public Object onTimeout() throws InterruptedException {
            //TODO 获得锁超时后要做的事
            return null;
        }
    });
}
~~~

##基于Redis实现的分布式速率限制器

限制的资源,可以是ip,用户id,订单id,手机号,等等.
* 例如限制一个手机号每分钟只能发1条短信.
* 例如限制一个手机号每10秒钟只能发起1次表单提交请求.
* 例如限制一个ip地址每秒钟只能访问10次特定的资源.

~~~ java
public class AccessSpeedLimitTest {
    @Test
    public void test1() throws InterruptedException {
        JedisPool jp=new JedisPool("127.0.0.1",6379);
        AccessSpeedLimit accessSpeedLimit=new AccessSpeedLimit(jp);
        SimpleDateFormat sdf=new SimpleDateFormat(" mm:ss");
        while(true){
            //10.0.0.1这个ip每1秒钟最多访问5次if块内代码
            if(accessSpeedLimit.tryAccess("10.0.0.1", 1,5)){
                System.out.println("yes"+sdf.format(new Date()));
            }else{
                System.out.println("no"+sdf.format(new Date()));
            }
            Thread.sleep(100);
        }
    }

    @Test
    public void test2() throws InterruptedException {
        JedisPool jp=new JedisPool("127.0.0.1",6379);
        final RedisDistributedLockTemplate template=new RedisDistributedLockTemplate(jp);
        LimitRule limitRule=new LimitRule();
        limitRule.setSeconds(1);
        limitRule.setLimitCount(5);
        limitRule.setLockCount(7);
        limitRule.setLockTime(2);
        AccessSpeedLimit accessSpeedLimit=new AccessSpeedLimit(jp);
        SimpleDateFormat sdf=new SimpleDateFormat(" mm:ss");
        while(true){
            //10.0.0.1这个ip每1秒钟最多访问5次if块内代码.1秒超过10次后,锁定2秒,2秒内无法访问.
            if(accessSpeedLimit.tryAccess("10.0.0.1",limitRule)){
                System.out.println("yes"+sdf.format(new Date()));
            }else{
                System.out.println("no"+sdf.format(new Date()));
            }
            Thread.sleep(100);
        }
    }
}
~~~

#技术交流:
www.chilangedu.com 