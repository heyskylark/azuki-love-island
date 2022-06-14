import { useEffect, useState } from "react";
import { toast } from "react-toastify";
import { getLatestSeasonParticipants } from "../clients/MainClient";
import Footer from "../components/Footer";
import GalleryCard from "../components/GalleryCard";
import GalleryPreview from "../components/GalleryPreview";
import ParticipantResponse from "../models/api/ParticipantResponse";

function Gallery() {
    const [loading, setLoading] = useState<boolean>(true);
    const [modalClosed, setModalClosed] = useState<boolean>(true);

    const [participants, setParticipants] = useState<ParticipantResponse[]>([]);

    const [azukiId, setAzukiId] = useState<number>(-1);
    const [twitterHandle, setTwitterHandle] = useState<string>("");
    const [color, setColor] = useState<string>("");
    const [imageUrl, setImageUrl] = useState<string>("");
    const [bio, setBio] = useState<string>("");
    const [hobbies, setHobbies] = useState<string[]>([]);

    useEffect(() => {
        async function getParticipants() {
            try {
                const participantsResponse = await getLatestSeasonParticipants()
                const data = participantsResponse.data

                setParticipants(data);
            } catch (err) {
                toast.error("There was a problem loading the participants...")
            } finally {
                setLoading(false);
            }
        }

        getParticipants();
    }, []);

    function openModal(azukId: number, twitterHandle: string, color: string, imageUrl: string, bio: string | undefined, hobbies: string[] | undefined) {
        setAzukiId(azukId);
        setTwitterHandle(twitterHandle);
        setColor(color);
        setImageUrl(imageUrl);
        setBio(bio ? bio : "");
        setHobbies(hobbies ? hobbies : [])

        setModalClosed(false);
    }

    function closeModal() {
        setAzukiId(-1);
        setTwitterHandle("");
        setColor("");
        setImageUrl("");
        setBio("");
        setHobbies([])

        setModalClosed(true);
    }

    function renderModal() {
        if (!modalClosed) {
            return (
                <GalleryCard
                    azukiId={azukiId}
                    twitterHandle={twitterHandle}
                    color={color}
                    imageUrl={imageUrl}
                    bio={bio}
                    hobbies={hobbies}
                    closeModal={closeModal}
                />
            );
        }
    }

    function renderParticipants() {
        let preview: JSX.Element[] = []
        participants.forEach(participant => {
            preview.push(
                <GalleryPreview
                    key={participant.azukiId}
                    azukiId={participant.azukiId}
                    twitterHandle={participant.twitterHandle}
                    color={participant.backgroundTrait}
                    imageUrl={participant.imageUrl}
                    modalImageUrl={participant.imageUrl}
                    bio={participant.bio}
                    hobbies={participant.hobbies}
                    openModal={openModal}
                />
            )
        });

        return preview;
    }

    return (
        <>
        <div className="container mx-auto">
            <div className="max-w-11xl mx-auto px-4 sm:px-6 lg:px-8">
                <section className="pb-24">
                <div className="w-full pt-28 pb-7 lg:pb-20">
                    <h1 className="uppercase font-black text-4xl lg:text-5xl whitespace-pre-line">Contestants&nbsp;<span className="opacity-10"> //</span></h1>
                </div>
                    <div className="flex">
                        <div className="w-full">
                            <div className="grid lg:grid-cols-5 2xl:grid-cols-5 grid-cols-2 lg:gap-x-6 gap-x-4 lg:gap-y-2 gap-y-1 lg:col-span-3">
                                {renderParticipants()}
                            </div>
                        </div>
                    </div>
                </section>
            </div>
        </div>

        {renderModal()}

        <Footer footerType={2} />
        </>
    );
}

export default Gallery;
