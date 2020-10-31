package blockingqueue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Blocking_Queue<T> {

	private final Lock lock = new ReentrantLock();
	private final Condition full_condition = lock.newCondition();
	private final Condition empty_condition = lock.newCondition();

	private final T thebuffer[];
	private int in, out, slotPieni;

	@SuppressWarnings("unchecked")
	public Blocking_Queue(final int dim) {

		in = 0;
		out = 0;
		slotPieni = 0;
		thebuffer = (T[]) new Object[dim];
	}

	public void put(final T c) throws InterruptedException {

		lock.lock();
		try {
			while (slotPieni == thebuffer.length)
				try {
					full_condition.await();
				} catch (final InterruptedException e) {

				}

			thebuffer[in] = c;
			in = (in + 1) % thebuffer.length;
			if (slotPieni == 0)
				empty_condition.signal();
			slotPieni++;

			show();
		} finally {
			lock.unlock();
		}

	}

	@SuppressWarnings("unused")
	private void show() {
		final char[] val = new char[thebuffer.length];
		for (int i = 0; i < slotPieni; i++)
			val[(out + i) % thebuffer.length] = '*';
		for (int i = 0; i < thebuffer.length - slotPieni; i++)
			val[(in + i) % thebuffer.length] = '-';
		System.out.print("In:" + in + " Out:" + out + " C:" + slotPieni + " ");
		System.out.println(val);
	}

	public T take() throws InterruptedException {

		T returnValue;
		lock.lock();
		try {
			while (slotPieni == 0)
				try {
					empty_condition.await();
				} catch (final InterruptedException e) {
				}

			returnValue = thebuffer[out];
			out = (out + 1) % thebuffer.length;
			if (slotPieni == thebuffer.length)
				full_condition.signal();
			slotPieni--;
			show();
			return returnValue;
		} finally {
			lock.unlock();
		}

	}

}

class Consumer extends Thread {

	private final Blocking_Queue<String> queue;

	public Consumer(final Blocking_Queue<String> buffer) {
		queue = buffer;
	}

	@Override
	public void run() {

		while (true)
			try {
				Thread.sleep((int) (Math.random() * 2000));

				System.out.println("Consumer " + getName() + " awakes ");
				System.out.println("Consumer " + getName() + " gets \"" +
				queue.take()+ "\"");
				queue.take();

			} catch (final InterruptedException e) {
				throw new RuntimeException(e);
			}
	}

}

class ProdConsEmptyFullCondition {

	public static void main(final String[] args) {

		final Producer p[] = new Producer[2];
		final Consumer c[] = new Consumer[3];

		final Blocking_Queue<String> buffer = new Blocking_Queue<>(10);
		for (int i = 0; i < 2; i++) {
			p[i] = new Producer(buffer);
			p[i].start();
		}
		for (int i = 0; i < 3; i++) {
			c[i] = new Consumer(buffer);
			c[i].start();
		}
		

	}
}

class Producer extends Thread {

	private final Blocking_Queue<String> queue;

	public Producer(final Blocking_Queue<String> buffer) {
		queue = buffer;
	}

	@Override
	public void run() {

		while (true)
			try {

				Thread.sleep((int) (Math.random() * 2000));

				System.out.println("Producer " + getName() + " awakes ");

				queue.put(getName());

				 System.out.println("Producer " + getName() + " puts \"" + getName() + "\"");

			} catch (final InterruptedException e) {
				throw new RuntimeException(e);
			}
	}
}