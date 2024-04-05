package ai.mr.pipeline;

public class StubProducerConsumerErrorHandler implements ProducerConsumerErrorHandler {

	@Override
	public void handle(Exception e) {
		e.printStackTrace();
	}
}
