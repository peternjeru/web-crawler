package ke.co.proxyapi.crawler.persistence.models;

import lombok.*;
import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "websites")
public class Website
{
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(nullable = false)
    private String urlHash;

    @Column(nullable = false)
    private Boolean processed = false;
}
