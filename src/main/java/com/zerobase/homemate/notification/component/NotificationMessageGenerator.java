package com.zerobase.homemate.notification.component;

import com.zerobase.homemate.entity.User;
import com.zerobase.homemate.notification.model.NotificationIndex;
import com.zerobase.homemate.util.KoreanPostpositionUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class NotificationMessageGenerator {

    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    private final List<String> randomMessages = List.of(
            "오늘도 홈메이트와 함께 일상을 만들어봐요",
            "오늘도 홈메이트와 함께 루틴을 완성해봐요",
            "오늘도 홈메이트와 함께 차근차근 해봐요",
            "홈메이트가 %s님의 집안일을 응원해요",
            "홈메이트가 %s님의 일상을 응원해요",
            "홈메이트와 함께 차근차근 채워봐요",
            "%s님의 집안일을 홈메이트와 함께 해요!"
    );

    public String titleFor(NotificationIndex index) {
        return switch (index) {
            case T_MINUS_1D -> "내일 집안일이 예정되어 있어요";
            case T_MINUS_10M -> "10분 후에 집안일을 할 시간이에요";
            case T_0 -> "지금부터 집안일을 해볼까요?";
        };
    }

    public String titleFor(String index) {
        NotificationIndex i = NotificationIndex.fromIndex(index);
        if (i == null) {
            return titleFor(NotificationIndex.T_0);
        }

        return titleFor(i);
    }

    public String buildMessage(User user, String choreTitle) {
        String first = String.format("%s%s 준비되셨나요?",
                choreTitle,
                KoreanPostpositionUtils.selectIGa(choreTitle)
        );

        String secondTemplate = randomMessages.get(random.nextInt(randomMessages.size()));
        String second;
        if (secondTemplate.contains("%s")) {
            String username = user.getProfileName();
            second = String.format(secondTemplate, username);
        } else {
            second = secondTemplate;
        }

        return first + " " + second;
    }
}
