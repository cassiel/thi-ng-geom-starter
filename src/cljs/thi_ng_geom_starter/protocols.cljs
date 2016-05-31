(ns thi-ng-geom-starter.protocols)

(defprotocol APP
  (init-app [_]
    "Initialise application. Returns a hash of properties (to be put into
     an atom for the app state)." )

  (update-app [_ component app-state t frame]
    "Frame refresh, referring to app contents and component. Return
     truthy value to indicate continued animation. (Assumes we don't
     need to change the app state.)" )

  (resize-app [_ app-state]
    "Rebuild viewport. Take dereferenced application state, return new
     state (with new viewport settings)." ))
