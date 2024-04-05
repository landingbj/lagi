package ai.lagi.utils;

import io.reactivex.Observable;
import io.reactivex.subjects.ReplaySubject;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObservableList<T> {
    private final List<T> list;
    private final ReplaySubject<T> subject;

    public ObservableList() {
        // 使用线程安全的列表
        this.list = new CopyOnWriteArrayList<>();
        // 使用ReplaySubject以便新订阅者也能接收到所有历史元素
        this.subject = ReplaySubject.create();
    }

    public synchronized void add(T value) {
        list.add(value);
        subject.onNext(value);
    }

    public synchronized void remove(T value) {
        if (list.remove(value)) {
            subject.onNext(value); // 或者使用另一个Subject通知移除事件
        }
    }

    public Observable<T> getObservable() {
        return subject.hide(); // hide()方法可以防止强制转换回Subject，以避免外部调用onNext等方法
    }

    // 提供获取当前列表的不可修改视图，以避免外部修改
    public List<T> getList() {
        return Collections.unmodifiableList(list);
    }

    public void onComplete() {
        subject.onComplete();
    }
}
