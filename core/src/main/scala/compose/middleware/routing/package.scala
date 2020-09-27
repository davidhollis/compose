package compose.middleware

import compose.Application

package object routing {

  implicit class PatternRuleArrow[Body](pattern: Pattern[Body]) {

    def |->(
      app: Application[Body]
    ): PatternRule[Body] = new PatternRule[Body](pattern, app)

  }

}
