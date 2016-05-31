(ns thi-ng-geom-starter.protocols)

(defprotocol APP
  (init-app [_ component A_app-state]
    "Initialise application. Takes the actual `app-state` atom since it might
     set up some callbacks which need to alter it (e.g. video streaming)." )

  (update-app [_ component A_app-state]
    "Frame refresh, referring to app contents and
     component. Higher-order: returns a function from `[t frame]` to
     truthy value to indicate continued animation." )

  (resize-app [_ app-state]
    "Rebuild viewport. Take dereferenced application state, return new
     state (with new viewport settings)." ))
