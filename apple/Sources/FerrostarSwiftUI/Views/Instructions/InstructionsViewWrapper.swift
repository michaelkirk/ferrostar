import FerrostarCore
import FerrostarCoreFFI
import SwiftUI

struct InstructionsViewWrapper: View {
    @Environment(\.navigationFormatterCollection) var formatterCollection: any FormatterCollection

    let navigationState: NavigationState?
    let isExpanded: Binding<Bool>
    let sizeWhenNotExpanded: Binding<CGSize>

    var body: some View {
        if case .navigating = navigationState?.tripState,
           let progress = navigationState?.currentProgress,
           let remainingSteps = navigationState?.remainingSteps,
           // REVIEW: is this still necesarry?
           // FIXME: without this long stretches of the trip show no banner.
           // This seems to be a difference with travelmux vs. stadia
           let visualInstruction = (navigationState?.currentVisualInstruction ?? remainingSteps
               .compactMap(\.visualInstructions.first).first)
        {
            InstructionsView(
                visualInstruction: visualInstruction,
                distanceFormatter: formatterCollection.distanceFormatter,
                distanceToNextManeuver: progress.distanceToNextManeuver,
                remainingSteps: remainingSteps,
                isExpanded: isExpanded,
                sizeWhenNotExpanded: sizeWhenNotExpanded
            )
        }
    }
}
