import { useEffect } from "react";
import ReactGA from "react-ga4";
import { useLocation } from "react-router-dom";

function usePageTracking() {
    const location = useLocation();

    useEffect(() => {
        const measurmentId = process.env.REACT_APP_G_ANALYTICS_MEASUREMENT_ID;

        if (measurmentId) {
            ReactGA.initialize(measurmentId);
            ReactGA.send({ hitType: "pageview", page: location.pathname + location.search });
        } else {
            console.log("G Measurment ID not found")
        }
    }, [location]);
}

export default usePageTracking;
