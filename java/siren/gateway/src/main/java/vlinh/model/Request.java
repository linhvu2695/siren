package vlinh.model;

import lombok.*;
import org.springframework.http.HttpMethod;

import javax.persistence.*;

@Data
@Builder
@Entity
@Table(name = "requests")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Request {
    @Id
    @SequenceGenerator(
            name = "request_id_sequence",
            sequenceName = "request_id_sequence"
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "request_id_sequence"
    )
    private Integer id;

    @Column(columnDefinition = "VARCHAR(255)")
    @Convert(converter = HttpMethodConverter.class)
    private HttpMethod method;

    private String content;

    private String type;
}
