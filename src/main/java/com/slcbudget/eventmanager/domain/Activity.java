package com.slcbudget.eventmanager.domain;

import java.math.BigDecimal;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Activity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  private String description;

  @NotNull
  private BigDecimal value;

  @JsonIgnore
  @OneToMany(mappedBy = "activity", cascade = CascadeType.PERSIST)
  private Set<ActivityParticipants> participants;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "event_id")
  private Event event;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }

    if (o.getClass() != Activity.class) {
      return false;
    }

    Activity activity = (Activity) o;
    return activity.getId().equals(this.id) &&
        activity.getDescription().equals(this.description);
  }

}
