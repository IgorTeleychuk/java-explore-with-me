package ru.practicum.main_service.event.dto.UpdateEventRequest;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.main_service.event.enums.StateActionAdmin;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder(builderMethodName = "adminBuilder")
public class UpdateEventAdminRequest extends UpdateEventRequest {
    StateActionAdmin stateAction;
}
