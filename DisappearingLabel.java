import nz.sodium.*;
import nz.sodium.time.MillisecondsTimerSystem;
import nz.sodium.time.TimerSystem;
import swidgets.SButton;
import swidgets.SLabel;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public class DisappearingLabel {
    public static void main(String[] args) {
        TimerSystem sys = new MillisecondsTimerSystem();
        Cell<Long> time = sys.time;
        StreamSink<Unit> sTick = new StreamSink<>();
        long t0 = System.currentTimeMillis();

        JFrame frame = new JFrame("Disappearing Label");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        SButton button = new SButton("Set");
        frame.add(button);

        Transaction.runVoid(
                ()->{
                    CellLoop<Long> tClick = new CellLoop<>();
                    Stream<Long> sClickedTime = button.sClicked.map(u->time.sample());
                    Stream<String> sClickedLabel = button.sClicked.map(u->"Hello");

                    Stream<Unit> sElapsed = Stream.filterOptional(
                        sTick.snapshot(tClick, (u,tClick_)-> time.sample() - tClick_ >= 3000
                                    ? Optional.of(Unit.UNIT)
                                    : Optional.empty()
                        )
                    );
                    Stream<String> sClearLabel = sElapsed.map(u->"");
                    Stream<String> clickUpdateStream = sClickedLabel.orElse(sClearLabel);

                    SLabel tClickLabel = new SLabel(clickUpdateStream.hold(""));
                    tClick.loop(sClickedTime.hold(t0));

                    frame.add(tClickLabel);
                }
        );

        frame.setSize(400,160);
        frame.setVisible(true);

        long tLast = t0;
        while (true) {
            long t = System.currentTimeMillis();
            long tIdeal = tLast + 20;
            long toWait = tIdeal - t;
            if (toWait > 0)
                try { Thread.sleep(toWait); } catch (InterruptedException e) {}
            sTick.send(Unit.UNIT);
            tLast = tIdeal;
        }
    }
}
