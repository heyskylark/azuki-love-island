import { AxiosError } from "axios";
import { useState } from "react";
import { toast } from "react-toastify";
import { claimPoap } from "../../clients/MainClient";
import { SmallLoading } from "../Loading";

interface Props {
    seasonNumber: number;
    twitterHandle?: string;
}

function PoapClaim(props: Props) {
    const [loading, setLoading] = useState<boolean>(false);
    const [poapLink, setPOAPLink] = useState<string>("");

    async function claimPoapRequest(e: { preventDefault: () => void }) {
        e.preventDefault();

        if (props.twitterHandle) {
            setLoading(true);

            try {
                const poapResponse = await claimPoap(props.seasonNumber, props.twitterHandle);
                const generatedPoap = poapResponse.data;

                setPOAPLink(generatedPoap.url);
            } catch (err) {
                if (err instanceof AxiosError && err.response) {
                    const errMessage = err.response?.data?.message ? err.response?.data?.message : "There was a problem generating your POAP.";
                    toast.error(errMessage);
                } else {
                    console.log(err);
                    toast.error("There was a problem generating your POAP.");
                }
            } finally {
                setLoading(false);
            }
        } else {
            console.log("Twitter handle missing...");
        }
    }

    function renderPOAPButton(): JSX.Element {
        if (loading) {
            return <SmallLoading />;
        } else if (poapLink.length > 0) {
            return <div className="mb-6"><a className="underline text-base md:text-2xl" href={poapLink}>{poapLink}</a></div>
        } else {
            return (
                <button onClick={claimPoapRequest} className="mb-6 px-4 py-3 uppercase font-bold text-lg bg-azukired text-white rounded disabled:opacity-70" disabled={!props.twitterHandle}>
                    Generate Link
                </button>
            )
        }
    }

    function renderPoapWindow(): JSX.Element {
        return (
            <div className="w-full flex flex-wrap justify-center lg:text-center text-center">
                <div className="w-full lg:w-1/2 ">
                    <img className="mb-2" src="images/girl.png" alt='' />
                    <h1 className="mb-6 uppercase font-black text-2xl lg:text-5xl whitespace-pre-line">
                        Claim Your POAP Now:
                    </h1>
                    {renderPOAPButton()}
                    <p className="w-full text-xs md:text-base">** Be sure to save the link once you generate **<br />you will not be able to generate a second time</p>
                </div>
            </div>
        );
    }

    return renderPoapWindow();
}

export default PoapClaim;
