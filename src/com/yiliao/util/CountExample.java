package com.yiliao.util;

import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sf.json.JSONObject;

public class CountExample {

	static int count = 0;
	// 总访问量是clientNum，并发量是threadNum
	int threadNum = 10;
	int clientNum = 300;

	float avgExecTime = 0;
	float sumexecTime = 0;
	long firstExecTime = Long.MAX_VALUE;
	long lastDoneTime = Long.MIN_VALUE;
	float totalExecTime = 0;
	
	
	final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDok5fqwPDwDjF2ne6c2oNpRyg2HzSN3H3jeSzg8RRD53FZ0X5qbYDPeOyMs52PUg+8VhPleHpNIQrU/1pVt1KW8Mlk1Oyo8AtYWTyxKqjRVD8HZFCeY8/Nav1D/y0s3GuVLJOsiNASFCPja528UDM7WGSMLUfiYZkHh5FoONOzbQIDAQAB";
	final String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAOiTl+rA8PAOMXad7pzag2lHKDYfNI3cfeN5LODxFEPncVnRfmptgM947IyznY9SD7xWE+V4ek0hCtT/WlW3UpbwyWTU7KjwC1hZPLEqqNFUPwdkUJ5jz81q/UP/LSzca5Usk6yI0BIUI+NrnbxQMztYZIwtR+JhmQeHkWg407NtAgMBAAECgYBq/gMELtBoTs84dz0fEXMkymRYSZC+tjF/pO4daSpedqlxnWtfgJKiX0nLDJIuLZ0pkhvDE+KHeuzlwbcH+bbW8teOT3S+k9i/+U1h4MWJgjv0i3CWf/DbWhJ6y1YlscQfrm2KZdCbuW54n6AS1SvVf9bBTSAxvpKS6jmq93HtwQJBAPa9oELb/eMjLxNeETcI8a5CZq6mjIuTiqjI5CkyJfvmOi1TPbmGSPB5RxmvvWwGPnYk9SYVOy+rNKGPyigxC90CQQDxTeVNy7Pie3V7C/dQ3vO7X0NSC39vgskuyn2f2ZR1mg2YcFdp3qgZkZy34rcsQpEQ2IGOE2PG9pDJuc5abtTRAkBWhmzaxVak/kOV4RjcWdCWsUZc3J7Qm262faw1Hhbf3P5twpEUrBiL65uZUF12skHZIGCveCaHMtyEA2565agpAkEA49QNHU+oDr1MJZodruBiNVXzZOJwNqPAOYp749H1xrdmALiI/+92vXVrB39qPMK43rPcVn1eJnukJqJk/6NHEQJARuf2jjR3udy6Km+LmAFNkbET14PGL/ThzZX2swxz6b86UCIpbbzY293x8vtkNRzePv/u089HDtWAbD+gXK/+kw==";
	

	public static void main(String[] args) {
		new CountExample().run();
		System.out.println("finished!");
	}

	public void run() {

		final ConcurrentHashMap<Integer, ThreadRecord> records = new ConcurrentHashMap<Integer, ThreadRecord>();

		// 建立ExecutorService线程池，threadNum个线程可以同时访问
		ExecutorService exec = Executors.newFixedThreadPool(threadNum);
		// 模拟clientNum个客户端访问
		final CountDownLatch doneSignal = new CountDownLatch(clientNum);

		for (int i = 0; i < clientNum; i++) {
			Runnable run = new Runnable() {
				public void run() {
					int index = getIndex();
					long systemCurrentTimeMillis = System.currentTimeMillis();
					try {
						JSONObject json = new JSONObject();
						json.put("userId", 108);
						json.put("page", 1);
						json.put("searchType", 0);
						String encodedData = RSACoderUtil.publicEncrypt(json.toString(), RSACoderUtil.getPublicKey(publicKey));
						String sendGet =  HttpUtil.httpClentString("http://47.75.212.233/app/getOnLineUserList.html", "param="+URLEncoder.encode(encodedData, "UTF-8"));
						System.out.println("返回数据->"+sendGet);

					} catch (Exception e) {
						e.printStackTrace();
					}
					records.put(index, new ThreadRecord(systemCurrentTimeMillis, System.currentTimeMillis()));
					doneSignal.countDown();// 每调用一次countDown()方法，计数器减1
				}
			};
			exec.execute(run);
		}

		try {
			// 计数器大于0 时，await()方法会阻塞程序继续执行
			doneSignal.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		/**
		 * 获取每个线程的开始时间和结束时间
		 */
		for (int i : records.keySet()) {
			ThreadRecord r = records.get(i);
			sumexecTime += ((double) (r.endTime - r.startTime)) / 1000;

			if (r.startTime < firstExecTime) {
				firstExecTime = r.startTime;
			}
			if (r.endTime > lastDoneTime) {
				this.lastDoneTime = r.endTime;
			}
		}

		this.avgExecTime = this.sumexecTime / records.size();
		this.totalExecTime = ((float) (this.lastDoneTime - this.firstExecTime)) / 1000;
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(4);

		System.out.println("======================================================");
		System.out.println("线程数量:\t\t" + threadNum);
		System.out.println("客户端数量:\t" + clientNum);
		System.out.println("平均执行时间:\t" + nf.format(this.avgExecTime) + "秒");
		System.out.println("总执行时间:\t" + nf.format(this.totalExecTime) + "秒");
		System.out.println("吞吐量:\t\t" + nf.format(this.clientNum / this.totalExecTime) + "次每秒");
	}

	public static int getIndex() {
		return ++count;
	}

}

class ThreadRecord {
	long startTime;
	long endTime;

	public ThreadRecord(long st, long et) {
		this.startTime = st;
		this.endTime = et;
	}

}
