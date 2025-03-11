package org.apache.rocketmq.example.transaction.propagattion;//package cn.thinkinjava.main.transaction.propagation;
//
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
///**
// * Spring事务传播
// * 七种类型分别是：REQUIRED、SUPPORTS、MANDATORY、REQUIRES_NEW、NOT_SUPPORTED、NEVER、NESTED
// *
// * Spring中事务的默认实现使用的是AOP，也就是代理的方式，如果大家在使用代码测试时，同一个Service类中的方法相互调用需要使用注入的对象来调用，
// * 不要直接使用this.方法名来调用，this.方法名调用是对象内部方法调用，不会通过Spring代理，也就是事务不会起作用
// *
// * 参考：https://zhuanlan.zhihu.com/p/148504094
// *
// * @author qiuxianbao
// * @date 2024/02/27
// * @since ace_1.4.0_20240109
// */
//public class TransactionPropagation {
//
//    /**
//     * NESTED
//     *
//     * 结果是a1,a2存储成功，b1和b2存储失败，因为调用方catch了被调方的异常，所以只有子事务回滚了。
//     */
//    class Case71 {
//        @Transactional(propagation = Propagation.REQUIRED)
//        public void testMain(){
//            A a1 = new A();
//            A(a1);  //调用A入参a1
//            try{
//                testB();    //调用testB
//            } catch (Exception e){
//
//            }
//            A a2 = new A();
//            A(a2);
//        }
//
//        @Transactional(propagation = Propagation.NESTED)
//        public void testB(){
//            B b1 = new B();
//            B(b1);  //调用B入参b1
//            try {
//                int i = 1/0;
//            } catch (Exception e) {
//                throw new RuntimeException();     //发生异常抛出
//            }
//            B b2 = new B();
//            B(b2);  //调用B入参b2
//        }
//    }
//
//    /**
//     * NESTED
//     * 如果当前事务存在，则在嵌套事务中执行，否则REQUIRED的操作一样（开启一个事务）
//     *
//     * 这里需要注意两点：
//     *
//     * 1、和 REQUIRES_NEW 的区别
//     * REQUIRES_NEW是新建一个事务并且新开启的这个事务与原有事务无关，
//     * 而 NESTED则是当前存在事务时（我们把当前事务称之为父事务）会开启一个嵌套事务（称之为一个子事务）。
//     * 在 NESTED情况下【父事务回滚时，子事务也会回滚】，而在REQUIRES_NEW情况下，原有事务回滚，不会影响新开启的事务。
//     *
//     * 2、和 REQUIRED 的区别
//     * REQUIRED情况下，调用方存在事务时，则被调用方和调用方使用同一事务，那么被调用方出现异常时，由于共用一个事务，所以无论调用方是否catch其异常，事务都会回滚
//     * 而在 NESTED 情况下，被调用方发生异常时，调用方可以 catch 其异常，这样只有【子事务回滚，父事务不受影响】
//     *
//     * 该场景下，所有数据都不会存入数据库，
//     * 因为在testMain发生异常时，父事务回滚则子事务也跟着回滚了，可以与(示例5)比较看一下，就找出了与REQUIRES_NEW的不同
//     *
//     * 总结：
//     * 当子方法是嵌套事务时，
//     * 父方法抛异常，子方法会回滚；子方法抛异常，父方法不会回滚
//     *
//     */
//    class Case7 {
//        @Transactional(propagation = Propagation.REQUIRED)
//        public void testMain(){
//            A a1 = new A();
//            A(a1);  //调用A入参a1
//            testB();    //调用testB
//            try {
//                int i = 1/0;
//            } catch (Exception e) {
//                throw new RuntimeException();     //发生异常抛出
//            }
//        }
//
//        @Transactional(propagation = Propagation.NESTED)
//        public void testB(){
//            B b1 = new B();
//            B(b1);  //调用B入参b1
//            B b2 = new B();
//            B(b2);  //调用B入参b2
//        }
//    }
//
//
//    /**
//     * NEVER
//     * 不使用事务，如果当前事务存在，则抛出异常
//     * 这个方法不使用事务，并且调用我的方法也不允许有事务，如果调用我的方法有事务则我直接抛出异常。
//     *
//     * 结果：
//     * 该场景执行，直接抛出事务异常，且不会有数据存储到数据库。
//     * 由于testMain事务传播类型为REQUIRED，所以testMain是运行在事务中，而testB事务传播类型为NEVER，
//     * 所以testB不会执行而是直接抛出事务异常，此时testMain检测到异常就发生了回滚，所以最终数据库不会有数据存入。
//     */
//    class Case6 {
//        @Transactional(propagation = Propagation.REQUIRED)
//        public void testMain(){
//            A a1 = new A();
//            A(a1);  //调用A入参a1
//            testB();    //调用testB
//        }
//
//        @Transactional(propagation = Propagation.NEVER)
//        public void testB(){
//            B b1 = new B();
//            B(b1);  //调用B入参b1
//            try {
//                int i = 1/0;
//            } catch (Exception e) {
//                throw new RuntimeException();     //发生异常抛出
//            }
//            B b2 = new B();
//            B(b2);  //调用B入参b2
//        }
//    }
//
//    /**
//     * NOT_SUPPORTED
//     * 始终以非事务方式执行,如果当前存在事务，则挂起当前事务
//     * 可以理解为在执行时，不论当前是否存在事务，都会以非事务的方式运行。
//     *
//     * 结果：
//     * 是a1和b2没有存储，而b1存储成功
//     *
//     * testMain有事务，而testB不使用事务，所以执行中testB的存储b1成功，然后抛出异常，此时testMain检测到异常事务发生回滚，
//     * 但是由于testB不在事务中，所以只有testMain的存储a1发生了回滚，最终只有b1存储成功，而a1和b1都没有存储
//     */
//    class Case5 {
//        @Transactional(propagation = Propagation.REQUIRED)
//        public void testMain(){
//            A a1 = new A();
//            A(a1);  //调用A入参a1
//            testB();    //调用testB
//        }
//
//        @Transactional(propagation = Propagation.NOT_SUPPORTED)
//        public void testB(){
//            B b1 = new B();
//            B(b1);  //调用B入参b1
//            try {
//                int i = 1/0;
//            } catch (Exception e) {
//                throw new RuntimeException();     //发生异常抛出
//            }
//            B b2 = new B();
//            B(b2);  //调用B入参b2
//        }
//    }
//
//    /**
//     * REQUIRES_NEW
//     * 创建一个新事务，如果存在当前事务，则挂起该事务。
//     *
//     * 总结：在执行时，不论当前是否存在事务，总是会新建一个事务。
//     *
//     * 结果：
//     * a1没有存储，而b1和b2存储成功
//     *
//     * 因为testB的事务传播设置为REQUIRES_NEW,所以在执行testB时会开启一个新的事务，
//     * testMain中发生的异常是在testMain所开启的事务中，所以这个异常不会影响testB的事务提交，
//     * testMain中的事务会发生回滚，所以最终a1就没有存储，而b1和b2就存储成功了。
//     *
//     * 与这个场景对比的一个场景就是testMain和testB都设置为REQUIRED，
//     * 那么上面的代码执行结果就是所有数据都不会存储，因为testMain和testMain是在同一个事务下的，所以事务发生回滚时，所有的数据都会回滚
//     */
//    class Case4 {
//        @Transactional(propagation = Propagation.REQUIRED)
//        public void testMain(){
//            A a1 = new A();
//            A(a1);  //调用A入参a1
//            testB();    //调用testB
//            try {
//                int i = 1/0;
//            } catch (Exception e) {
//                throw new RuntimeException();     //发生异常抛出
//            }
//        }
//
//        @Transactional(propagation = Propagation.REQUIRES_NEW)
//        public void testB(){
//            B b1 = new B();
//            B(b1);  //调用B入参b1
//
//            B b2 = new B();
//            B(b2);  //调用B入参b2
//        }
//    }
//
//    /**
//     * MANDATORY
//     *
//     * 在testMain方法进行事务声明，并且设置为REQUIRED，
//     * 则执行testB时就会使用testMain已经开启的事务，遇到异常就正常的回滚了。
//     */
//    class Case31 {
//        @Transactional(propagation = Propagation.REQUIRED)
//        public void testMain(){
//            A a1 = new A();
//            A(a1);  //调用A入参a1
//            testB();    //调用testB
//        }
//
//        @Transactional(propagation = Propagation.MANDATORY)
//        public void testB(){
//            B b1 = new B();
//            B(b1);  //调用B入参b1
//            try {
//                int i = 1/0;
//            } catch (Exception e) {
//                throw new RuntimeException();     //发生异常抛出
//            }
//            B b2 = new B();
//            B(b2);  //调用B入参b2
//        }
//    }
//
//    /**
//     * MANDATORY
//     *
//     * 当前存在事务，则加入当前事务；如果当前事务不存在，则抛出异常。
//     *
//     * 结果：
//     * 是a1存储成功，而b1和b2没有存储。
//     *
//     * b1和b2没有存储，并不是事务回滚的原因，而是因为testMain方法没有声明事务，
//     * 在去执行testB方法时就直接抛出事务要求的异常（如果当前事务不存在，则抛出异常），所以testB方法里的内容就没有执行。
//     *
//     */
//    class Case3 {
//        public void testMain(){
//            A a1 = new A();
//            A(a1);  //调用A入参a1
//            testB();    //调用testB
//        }
//
//        @Transactional(propagation = Propagation.MANDATORY)
//        public void testB(){
//            B b1 = new B();
//            B(b1);  //调用B入参b1
//            try {
//                int i = 1/0;
//            } catch (Exception e) {
//                throw new RuntimeException();     //发生异常抛出
//            }
//            B b2 = new B();
//            B(b2);  //调用B入参b2
//        }
//    }
//
//    /**
//     * SUPPORTS
//     *
//     * 结果：
//     * 这个时候执行testB就满足当前存在事务，则加入当前事务，
//     * 在testB抛出异常时事务就会回滚，最终结果就是a1，b1和b2都不会存储到数据库
//     *
//     */
//    class Case21 {
//
//        @Transactional(propagation = Propagation.REQUIRED)
//        public void testMain(){
//            A a1 = new A();
//            A(a1);  //调用A入参a1
//            testB();    //调用testB
//        }
//
//        @Transactional(propagation = Propagation.SUPPORTS)
//        public void testB(){
//            B b1 = new B();
//            B(b1);  //调用B入参b1
//            try {
//                int i = 1/0;
//            } catch (Exception e) {
//                throw new RuntimeException();     //发生异常抛出
//            }
//            B b2 = new B();
//            B(b2);  //调用B入参b2
//        }
//    }
//
//    /**
//     * SUPPORTS
//     * 当前存在事务，则加入当前事务；如果当前没有事务，就以非事务方法执行
//     *
//     * 结果：
//     * a1，b1存入数据库，b2没有存入数据库
//     *
//     * testMain没有声明事务，且testB的事务传播行为是SUPPORTS，所以执行testB时就是没有事务的（如果当前没有事务，就以非事务方法执行）
//     *
//     */
//    class Case2 {
//        public void testMain(){
//            A a1 = new A();
//            A(a1);  //调用A入参a1
//            testB();    //调用testB
//        }
//
//        @Transactional(propagation = Propagation.SUPPORTS)
//        public void testB(){
//            B b1 = new B();
//            B(b1);  //调用B入参b1
//            try {
//                int i = 1/0;
//            } catch (Exception e) {
//                throw new RuntimeException();     //发生异常抛出
//            }
//            B b2 = new B();
//            B(b2);  //调用B入参b2
//        }
//    }
//
//    /**
//     * REQUIRED(Spring默认的事务传播类型)
//     *
//     * 结果：
//     * 数据a1存储成功，数据b1和b2没有存储。
//     *
//     * 由于testMain没有声明事务，testB有声明事务且传播行为是REQUIRED，
//     * 所以在执行testB时会自己新建一个事务（如果当前没有事务，则自己新建一个事务），
//     * testB抛出异常则只有testB中的操作发生了回滚，也
//     * 就是b1的存储会发生回滚，但a1数据不会回滚，所以最终a1数据存储成功，b1和b2数据没有存储
//     */
//    class Case11 {
//        public void testMain(){
//            A a1 = new A();
//            A(a1);  //调用A入参a1
//            testB();    //调用testB
//        }
//
//        @Transactional(propagation = Propagation.REQUIRED)
//        public void testB(){
//            B b1 = new B();
//            B(b1);  //调用B入参b1
//            try {
//                int i = 1/0;
//            } catch (Exception e) {
//                throw new RuntimeException();     //发生异常抛出
//            }
//            B b2 = new B();
//            B(b2);  //调用B入参b2
//        }
//    }
//
//    /**
//     * REQUIRED(Spring默认的事务传播类型)
//     * 判断调用方，如果当前存在事务，则加入这个事务；如果当前没有事务，则自己新建一个事务。
//     *
//     * 结果：
//     * 数据库没有插入新的数据，数据库还是保持着执行testMain方法之前的状态，没有发生改变。
//     *
//     * testMain上声明了事务，在执行testB方法时就加入了testMain的事务（当前存在事务，则加入这个事务），
//     * 在执行testB方法抛出异常后事务会发生回滚，又testMain和testB使用的同一个事务，
//     * 所以事务回滚后testMain和testB中的操作都会回滚，也就使得数据库仍然保持初始状态
//     */
//    class Case1 {
//        @Transactional(propagation = Propagation.REQUIRED)
//        public void testMain(){
//            A a1 = new A();
//            A(a1);  //调用A入参a1
//            testB();    //调用testB
//        }
//
//        @Transactional(propagation = Propagation.REQUIRED)
//        public void testB(){
//            B b1 = new B();
//            B(b1);  //调用B入参b1
//            try {
//                int i = 1/0;
//            } catch (Exception e) {
//                throw new RuntimeException();     //发生异常抛出
//            }
//            B b2 = new B();
//            B(b2);  //调用B入参b2
//        }
//    }
//
//    /**
//     * 没有事务的场景
//     *
//     * 结果：
//     * a1数据成功存入ATable表，b1数据成功存入BTable表，而在抛出异常后b2数据存储就不会执行，也就是b2数据不会存入数据库
//     */
//    class Case {
//        public void testMain(){
//            A a1 = new A();
//            A(a1);  //调用A入参a1
//            testB();    //调用testB
//        }
//
//        public void testB(){
//            B b1 = new B();
//            B(b1);  //调用B入参b1
//            try {
//                int i = 1/0;
//            } catch (Exception e) {
//                throw new RuntimeException();     //发生异常抛出
//            }
//            B b2 = new B();
//            B(b2);  //调用B入参b2
//        }
//    }
//
//
//    public void A(A a){
//        //将传入参数a存入ATable
//        insertIntoATable(a);
//    }
//
//    //将传入参数b存入BTable
//    public void B(B b){
//        insertIntoBTable(b);
//    }
//
//    private void insertIntoATable(A a) {
//    }
//
//    private void insertIntoBTable(B b) {
//    }
//
//    class A {
//    }
//    class  B {
//    }
//
//}
