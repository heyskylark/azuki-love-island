import { createContext, useContext, useEffect, useState } from "react";
import { getLatestSeason } from "../clients/MainClient";
import Season from "../models/api/Season";

interface LatestSeasonContext {
    seasonContextLoading: boolean;
    latestSeason: Season | null;
}

const latestSeasonContext = createContext<LatestSeasonContext | null>(null);

interface ProvideLatestSeasonInterface {
    children: JSX.Element[];
}

export function ProvideLatestSeason(props: ProvideLatestSeasonInterface) {
    const latestSeason = useProvideLatestSeason();
    return <latestSeasonContext.Provider value={latestSeason}>{props.children}</latestSeasonContext.Provider>;
}
  
export const useLatestSeason = () => {
    return useContext(latestSeasonContext);
};

function useProvideLatestSeason(): LatestSeasonContext {
    const [seasonContextLoading, setSeasonContextLoading] = useState<boolean>(true);
    const [latestSeason, setLatestSeason] = useState<Season | null>(null);

    useEffect(() => {
        async function getInitData() {
            try {
                const latestSeasonResponse = await getLatestSeason();
                setLatestSeason(latestSeasonResponse.data);
            } catch (err) {
                console.log("There was a problem fetching the latest season: ", err)
            } finally {
                setSeasonContextLoading(false);
            }
        }

        getInitData();
    }, []);

    return {
        seasonContextLoading,
        latestSeason
    };
}
